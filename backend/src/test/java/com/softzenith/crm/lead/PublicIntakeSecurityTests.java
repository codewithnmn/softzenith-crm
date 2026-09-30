package com.softzenith.crm.lead;

import com.softzenith.crm.Fixtures;
import com.softzenith.crm.Fixtures.Staff;
import com.softzenith.crm.IntegrationTest;
import com.softzenith.crm.TestData;
import com.softzenith.crm.identity.DefaultRoles;
import com.softzenith.crm.tenancy.Tenant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Abuse resistance of the unauthenticated enquiry endpoint. */
@IntegrationTest
class PublicIntakeSecurityTests {

    @Autowired MockMvc mvc;
    @Autowired Fixtures fixtures;

    Tenant tenant;
    Staff admin;

    @BeforeEach
    void setUp() {
        tenant = fixtures.tenant();
        admin = fixtures.staff(tenant, DefaultRoles.ADMIN);
    }

    @Test
    void onePhoneNumberCannotBeUsedToFloodTheForm() throws Exception {
        var phone = TestData.phone();
        for (int i = 0; i < 5; i++) {
            submit("{\"fullName\":\"Asha\",\"phone\":\"" + phone + "\"}").andExpect(status().isCreated());
        }
        submit("{\"fullName\":\"Asha\",\"phone\":\"" + phone + "\"}")
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
        submit("{\"fullName\":\"Someone Else\",\"phone\":\"" + TestData.phone() + "\"}").andExpect(status().isCreated());
    }

    @Test
    void namesCannotCarryLinksOrLineBreaks() throws Exception {
        submit("{\"fullName\":\"https://evil.example/pay\",\"phone\":\"" + TestData.phone() + "\"}")
                .andExpect(status().isBadRequest());
        submit("{\"fullName\":\"Asha\\nURGENT: pay fees\",\"phone\":\"" + TestData.phone() + "\"}")
                .andExpect(status().isBadRequest());
        submit("{\"fullName\":\"Ana-Maria D'Souza Jr.\",\"phone\":\"" + TestData.phone() + "\"}")
                .andExpect(status().isCreated());
    }

    @Test
    void simultaneousDuplicateSubmissionsBecomeOneLeadAndNoEnquiryIsLost() throws Exception {
        var phone = TestData.phone();
        var body = "{\"fullName\":\"Double Click\",\"phone\":\"" + phone + "\",\"email\":\"dc@mail.test\"}";
        var tasks = new ArrayList<Callable<Integer>>();
        for (int i = 0; i < 4; i++) {
            tasks.add(() -> submit(body).andReturn().getResponse().getStatus());
        }
        try (var pool = Executors.newFixedThreadPool(4)) {
            var statuses = pool.invokeAll(tasks).stream().map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            }).collect(Collectors.toList());
            assertThat(statuses).containsOnly(201);
        }
        mvc.perform(get("/api/v1/leads").param("q", phone).with(admin.token()))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].enquiryCount").value(4));
    }

    @Test
    void customFieldsAreBounded() throws Exception {
        var fields = IntStream.range(0, 51).mapToObj(i -> "\"k" + i + "\":\"v\"").collect(Collectors.joining(","));
        mvc.perform(post("/api/v1/leads").with(admin.token()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Walk In\",\"phone\":\"" + TestData.phone()
                                + "\",\"sourceType\":\"WALK_IN\",\"customFields\":{" + fields + "}}"))
                .andExpect(status().isBadRequest());
    }

    private org.springframework.test.web.servlet.ResultActions submit(String body) throws Exception {
        return mvc.perform(post("/api/v1/public/tenants/{slug}/enquiries", tenant.getSlug())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }
}
