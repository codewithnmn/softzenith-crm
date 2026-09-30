package com.softzenith.crm.shared.logging;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.MDC;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** The access-log filter on its own: the branches a full request rarely reaches. */
@ExtendWith(OutputCaptureExtension.class)
class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();

    @Test
    void anExceptionEscapingTheAppIsLoggedAs500AndRethrown(CapturedOutput output) {
        var request = new MockHttpServletRequest("GET", "/api/v1/leads");
        var response = new MockHttpServletResponse();

        assertThatThrownBy(() -> filter.doFilter(request, response, (req, res) -> {
            throw new ServletException("container failure");
        })).isInstanceOf(ServletException.class);

        assertThat(output).contains("GET /api/v1/leads failed with an unhandled exception")
                .contains("GET /api/v1/leads -> 500");
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    void serverErrorsAreWarningsAndHealthChecksAreNotLoggedAtInfo(CapturedOutput output) throws Exception {
        filter.doFilter(new MockHttpServletRequest("GET", "/api/v1/x"), new MockHttpServletResponse(),
                (req, res) -> ((MockHttpServletResponse) res).setStatus(503));
        assertThat(output).contains("WARN").contains("GET /api/v1/x -> 503");

        filter.doFilter(new MockHttpServletRequest("GET", "/actuator/health"), new MockHttpServletResponse(), (req, res) -> {
        });
        assertThat(output.getAll().lines().filter(l -> l.contains("/actuator/health ->")))
                .allMatch(l -> l.contains("DEBUG"));
    }

    @Test
    void theResponseCarriesTheRequestId() throws Exception {
        var request = new MockHttpServletRequest("GET", "/api/v1/x");
        request.addHeader(LogContext.REQUEST_ID_HEADER, "caller-12345678");
        var response = new MockHttpServletResponse();
        filter.doFilter(request, response, (req, res) -> {
        });
        assertThat(response.getHeader(LogContext.REQUEST_ID_HEADER)).isEqualTo("caller-12345678");
    }
}
