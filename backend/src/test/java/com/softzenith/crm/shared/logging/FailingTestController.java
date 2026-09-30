package com.softzenith.crm.shared.logging;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Test-only endpoint that stands in for a bug anywhere in the code (see LoggingTests). */
@RestController
class FailingTestController {

    @GetMapping("/api/v1/public/test-only/boom")
    String boom() {
        throw new IllegalStateException("simulated bug");
    }
}
