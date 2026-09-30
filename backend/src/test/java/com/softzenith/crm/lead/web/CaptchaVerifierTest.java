package com.softzenith.crm.lead.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/** Turnstile is mocked at the HTTP boundary; nothing calls Cloudflare. */
class CaptchaVerifierTest {

    @Test
    void offWithoutASecret() {
        var verifier = new CaptchaVerifier("", RestClient.builder());
        assertThat(verifier.enabled()).isFalse();
        assertThat(verifier.verify(null, "10.0.0.1")).isTrue();
    }

    @Test
    void acceptsOnlyTokensTheProviderConfirms() {
        var http = RestClient.builder();
        var server = MockRestServiceServer.bindTo(http).build();
        var verifier = new CaptchaVerifier("secret", http);

        server.expect(requestTo(CaptchaVerifier.SITEVERIFY)).andExpect(method(HttpMethod.POST))
                .andExpect(content().formDataContains(java.util.Map.of("secret", "secret", "response", "good")))
                .andRespond(withSuccess("{\"success\":true}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(CaptchaVerifier.SITEVERIFY))
                .andRespond(withSuccess("{\"success\":false,\"error-codes\":[\"invalid-input-response\"]}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(CaptchaVerifier.SITEVERIFY)).andRespond(withServerError());

        assertThat(verifier.verify("good", "10.0.0.1")).isTrue();
        assertThat(verifier.verify("bad", "10.0.0.1")).isFalse();
        assertThat(verifier.verify("any", "10.0.0.1")).isFalse(); // provider down = not verified
        assertThat(verifier.verify(" ", "10.0.0.1")).isFalse();  // no call made
        server.verify();
    }
}
