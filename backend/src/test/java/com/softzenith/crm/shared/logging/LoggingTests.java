package com.softzenith.crm.shared.logging;

import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jayway.jsonpath.JsonPath;
import com.softzenith.crm.Fixtures;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DefaultRoles;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Every request can be traced: request id in the response and in error bodies, context in the log lines. */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class LoggingTests {

    @RegisterExtension
    static GreenMailExtension mail = new GreenMailExtension(ServerSetupTest.SMTP);

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    @Test
    void everyResponseCarriesARequestIdAndAWellFormedIncomingOneIsKept() throws Exception {
        mvc.perform(get("/api/v1/leads")).andExpect(header().exists(LogContext.REQUEST_ID_HEADER));
        mvc.perform(get("/api/v1/leads").header(LogContext.REQUEST_ID_HEADER, "website-abc12345"))
                .andExpect(header().string(LogContext.REQUEST_ID_HEADER, "website-abc12345"));
        mvc.perform(get("/api/v1/leads").header(LogContext.REQUEST_ID_HEADER, "bad id<script>"))
                .andExpect(header().string(LogContext.REQUEST_ID_HEADER, org.hamcrest.Matchers.not("bad id<script>")));
    }

    @Test
    void unexpectedErrorsAre500WithTheRequestIdAndAreLoggedWithTheStackTrace(CapturedOutput output) throws Exception {
        var result = mvc.perform(get("/api/v1/public/test-only/boom"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.detail", containsString("request id")))
                .andReturn();
        var requestId = result.getResponse().getHeader(LogContext.REQUEST_ID_HEADER);
        assertThat(JsonPath.<String>read(result.getResponse().getContentAsString(), "$.requestId")).isEqualTo(requestId);
        assertThat(result.getResponse().getContentAsString()).doesNotContain("simulated bug");
        assertThat(output).contains("500 unexpected error on GET /api/v1/public/test-only/boom")
                .contains("java.lang.IllegalStateException: simulated bug")
                .contains("[" + requestId + "|");
    }

    @Test
    void clientErrorsAndPermissionFailuresCarryTheRequestIdToo(CapturedOutput output) throws Exception {
        var tenant = fixtures.tenant();
        var counsellor = fixtures.staff(tenant, DefaultRoles.COUNSELLOR);
        mvc.perform(get("/api/v1/leads/{id}", UUID.randomUUID()).with(counsellor.token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
        mvc.perform(get("/api/v1/leads/stats").with(counsellor.token()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
        assertThat(output).contains("403 GET /api/v1/leads/stats")
                .contains("|" + tenant.getId() + "|" + counsellor.id() + "]");
    }

    @Test
    void intakeIsLoggedWithAMaskedPhoneAndTheRequestIdReachesTheBackgroundNotifications(CapturedOutput output) throws Exception {
        var tenant = fixtures.tenant();
        var phone = TestData.phone();
        var result = mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Log Test\",\"phone\":\"" + phone + "\",\"email\":\"log.test@mail.test\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        var requestId = result.getResponse().getHeader(LogContext.REQUEST_ID_HEADER);

        assertThat(output).contains("created via WEBSITE_FORM").contains(Mask.phone(phone));
        await().atMost(Duration.ofSeconds(15)).untilAsserted(() -> assertThat(output.getAll().lines()
                .filter(l -> l.contains("Email LEAD_WELCOME sent to l***@mail.test")))
                .anyMatch(l -> l.contains("[" + requestId + "|" + tenant.getId() + "|")));
        // Our own log lines (not test tooling such as GreenMail) never contain the full phone or email.
        assertThat(output.getAll().lines().filter(l -> l.contains("c.softzenith") || l.contains("com.softzenith") || l.contains("crm.access")))
                .noneMatch(l -> l.contains(phone) || l.contains("log.test@mail.test"));
    }
}
