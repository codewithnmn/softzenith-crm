package com.softzenith.crm.journeys;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.Fixtures.Staff;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DefaultRoles;
import com.softzenith.crm.tenancy.Tenant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PRD Phase 1, "Don't lose leads", as one story across the modules (lead intake, notifications, history, status):
 * every enquiry, from any source, ends up as exactly one open lead per person, staff hear about it, the student is
 * acknowledged, and nothing is lost when the same person comes back.
 *
 * <p>Read top to bottom; each step names the rule it proves. When a step fails, the per-endpoint tests
 * ({@code LeadEndpointTests}, {@code PublicEnquiryEndpointTests}, {@code NotificationRulesTests}) narrow it down.
 */
@IntegrationTest
class Phase1LeadCaptureJourneyTests {

    @RegisterExtension
    static GreenMailExtension mail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    @Test
    void anEnquiryIsCapturedAcknowledgedFollowedUpClosedAndNeverLost() throws Exception {
        Tenant tenant = fixtures.tenant();
        Staff admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
        Staff reception = fixtures.staff(tenant, DefaultRoles.RECEPTIONIST);
        var phone = TestData.phone();
        var email = "journey-" + phone.substring(6) + "@mail.test";

        // 1. The student fills in the website form.
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Riya Sharma","phone":"%s","email":"%s","message":"MS in Canada, Jan intake",
                                 "utmSource":"google"}""".formatted(phone.substring(3), email)))
                .andExpect(status().isCreated());

        // 2. It is a NEW, unassigned lead with a per-tenant number, visible to reception.
        var lead = only(reception, phone);
        var leadId = (String) lead.get("id");
        assertThat(lead.get("status")).isEqualTo("NEW");
        assertThat((String) lead.get("leadNumber")).startsWith("LD-");
        assertThat(lead.get("assignedTo")).isNull();

        // 3. The student is welcomed (email + WhatsApp) and admin + reception are alerted, straight away.
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(notifications(admin, leadId)).contains(
                "EMAIL/LEAD_WELCOME/" + email, "WHATSAPP/LEAD_WELCOME/" + phone,
                "EMAIL/NEW_LEAD_ALERT/" + admin.email(), "EMAIL/NEW_LEAD_ALERT/" + reception.email()));

        // 4. The same person walks in a week later: reception records it and gets the SAME lead back, moved to the top.
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Someone Else\",\"phone\":\"%s\"}".formatted(TestData.phone())));
        mvc.perform(post("/api/v1/leads").with(reception.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"fullName":"Riya Sharma","phone":"%s","email":"%s","sourceType":"WALK_IN"}""".formatted(phone, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(leadId))
                .andExpect(jsonPath("$.enquiryCount").value(2));
        mvc.perform(get("/api/v1/leads").with(reception.token()))
                .andExpect(jsonPath("$.items[0].id").value(leadId));

        // 5. Its history shows both enquiries, with their sources.
        mvc.perform(get("/api/v1/leads/{id}/activities", leadId).with(admin.token()))
                .andExpect(jsonPath("$[*].type", contains("CREATED", "REPEAT_ENQUIRY")))
                .andExpect(jsonPath("$[1].toValue").value("WALK_IN"));

        // 6. Admin follows up (CONTACTED), then closes it as lost with an internal reason.
        changeStatus(admin, leadId, "{\"status\":\"CONTACTED\"}").andExpect(jsonPath("$.status").value("CONTACTED"));
        changeStatus(admin, leadId, "{\"status\":\"CLOSED\",\"reason\":\"Chose another agency\"}")
                .andExpect(jsonPath("$.closedReason").value("Chose another agency"));

        // 7. The student hears about each step, but never the internal reason.
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(bodies(admin, leadId, "LEAD_STATUS_UPDATE"))
                .anyMatch(b -> b.contains("has been closed")));
        assertThat(bodies(admin, leadId, "LEAD_STATUS_UPDATE")).noneMatch(b -> b.contains("another agency"));

        // 8. Months later the same person enquires again: a closed lead is history, so a NEW lead is opened.
        mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug()).contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"Riya Sharma\",\"phone\":\"%s\",\"email\":\"%s\"}".formatted(phone, email)))
                .andExpect(status().isCreated());
        mvc.perform(get("/api/v1/leads").param("q", phone).with(admin.token()))
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/leads").param("q", phone).param("status", "NEW").with(admin.token()))
                .andExpect(jsonPath("$.totalElements").value(1));

        // 9. Reopening the old lead now would make two open leads for one person, so it is refused.
        changeStatus(admin, leadId, "{\"status\":\"NEW\"}")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail", containsString("is already open")));
    }

    private Map<String, Object> only(Staff as, String phone) throws Exception {
        var body = mvc.perform(get("/api/v1/leads").param("q", phone).with(as.token()))
                .andExpect(jsonPath("$.totalElements").value(1)).andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$.items[0]");
    }

    private org.springframework.test.web.servlet.ResultActions changeStatus(Staff as, String leadId, String body) throws Exception {
        return mvc.perform(post("/api/v1/leads/{id}/status", leadId).with(as.token())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private List<Map<String, Object>> rows(Staff as, String leadId) throws Exception {
        var body = mvc.perform(get("/api/v1/leads/{id}/notifications", leadId).with(as.token()))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(body, "$");
    }

    private List<String> notifications(Staff as, String leadId) throws Exception {
        return rows(as, leadId).stream().map(n -> n.get("channel") + "/" + n.get("kind") + "/" + n.get("recipient")).toList();
    }

    private List<String> bodies(Staff as, String leadId, String kind) throws Exception {
        return rows(as, leadId).stream().filter(n -> kind.equals(n.get("kind"))).map(n -> (String) n.get("body")).toList();
    }
}
