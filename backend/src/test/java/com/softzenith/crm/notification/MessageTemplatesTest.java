package com.softzenith.crm.notification;

import com.samskivert.mustache.Mustache;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.UncheckedIOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Template rendering without Spring. Test-only templates live in src/test/resources/templates/notifications. */
class MessageTemplatesTest {

    private final MessageTemplates templates = new MessageTemplates(Mustache.compiler(), new DefaultResourceLoader());

    @Test
    void aSubjectLineBecomesTheEmailSubject() {
        var m = templates.render("test-with-subject", Map.of("name", "Asha"));
        assertThat(m.subject()).isEqualTo("Hello Asha");
        assertThat(m.body()).isEqualTo("Body for Asha");
    }

    @Test
    void aTemplateThatIsOnlyASubjectHasAnEmptyBody() {
        var m = templates.render("test-subject-only", Map.of("name", "Asha"));
        assertThat(m.subject()).isEqualTo("Only Asha");
        assertThat(m.body()).isEmpty();
    }

    @Test
    void withoutASubjectLineItIsAPlainMessage() {
        var model = new java.util.HashMap<String, Object>();
        for (var key : java.util.List.of("leadNumber", "serviceInterest", "preferredCountry", "fullName", "phone", "email")) {
            model.put(key, null); // present but empty: renders as ""
        }
        model.put("firstName", "Asha");
        model.put("tenantName", "Acme");
        var m = templates.render("lead-welcome-whatsapp", model);
        assertThat(m.subject()).isNull();
        assertThat(m.body()).contains("Asha");
    }

    @Test
    void onlyTheMessageFieldMaySpanLinesAndNothingIsHtmlEscaped() {
        var m = templates.render("test-with-subject", Map.of("name", "Asha\nInjected & <b>"));
        assertThat(m.subject()).isEqualTo("Hello Asha Injected & <b>");
    }

    @Test
    void aMissingTemplateFailsClearly() {
        assertThatThrownBy(() -> templates.render("no-such-template", Map.of()))
                .isInstanceOf(UncheckedIOException.class)
                .hasMessageContaining("no-such-template");
    }
}
