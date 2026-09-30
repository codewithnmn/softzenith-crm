package com.softzenith.crm.shared.web;

import com.softzenith.crm.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Every exception type maps to the right RFC 9457 status, the body carries the request id, and nothing personal from
 * the exception (e.g. the duplicate phone in a database message) reaches the client or the log.
 */
@IntegrationTest
@ExtendWith(OutputCaptureExtension.class)
class ErrorHandlingTests {

    @Autowired MockMvc mvc;

    @Test
    void aConcurrentEditIs409AskingToReload() throws Exception {
        mvc.perform(get("/api/v1/public/test-only/stale"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Record was modified by someone else; reload and retry"))
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void aDatabaseConstraintIs409AndOnlyTheConstraintNameIsLogged(CapturedOutput output) throws Exception {
        var body = mvc.perform(get("/api/v1/public/test-only/integrity"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("Request conflicts with existing data"))
                .andReturn().getResponse().getContentAsString();

        assertThat(body).doesNotContain("+919999999999");
        assertThat(output).contains("constraint=leads_open_person_uq sqlState=23505").doesNotContain("+919999999999");
    }

    @Test
    void malformedJsonIs400() throws Exception {
        mvc.perform(post("/api/v1/public/test-only/echo").contentType(MediaType.APPLICATION_JSON).content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void aWrongMethodIs405() throws Exception {
        mvc.perform(put("/api/v1/public/test-only/echo").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
    }

    @Test
    void aFrameworkServerErrorIsLoggedAsAnError(CapturedOutput output) throws Exception {
        mvc.perform(get("/api/v1/public/test-only/timeout"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.requestId").isNotEmpty());
        assertThat(output).contains("ERROR").contains("503");
    }
}
