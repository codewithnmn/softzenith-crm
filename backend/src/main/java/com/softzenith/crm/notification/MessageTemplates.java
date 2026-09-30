package com.softzenith.crm.notification;

import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;
import com.softzenith.crm.shared.text.Text;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Platform-wide message templates ({@code templates/notifications/*.mustache}), shared by all tenants and
 * filled with tenant data (name, lead, links). A first line {@code Subject: ...} becomes the email subject.
 * Per-tenant overrides can be added later by looking up a tenant template before falling back to these.
 */
@Component
class MessageTemplates {

    record Message(String subject, String body) {
    }

    private final Mustache.Compiler compiler;
    private final ResourceLoader resources;
    private final Map<String, Template> cache = new ConcurrentHashMap<>();

    MessageTemplates(Mustache.Compiler compiler, ResourceLoader resources) {
        // Plain-text messages: no HTML escaping; missing values render empty.
        this.compiler = compiler.escapeHTML(false).defaultValue("").nullValue("");
        this.resources = resources;
    }

    Message render(String name, Map<String, Object> model) {
        var text = cache.computeIfAbsent(name, this::load).execute(clean(model)).strip();
        if (text.startsWith("Subject:")) {
            var newline = text.indexOf('\n');
            var subject = text.substring("Subject:".length(), newline < 0 ? text.length() : newline).strip();
            return new Message(subject, newline < 0 ? "" : text.substring(newline + 1).strip());
        }
        return new Message(null, text);
    }

    /**
     * Values come partly from the public form. Only {@code message} may span lines; any other line break would move
     * text out of the subject line into the body (or forge a line of the message).
     */
    private static Map<String, Object> clean(Map<String, Object> model) {
        var cleaned = new HashMap<String, Object>(model);
        cleaned.replaceAll((key, value) -> value instanceof String s
                ? ("message".equals(key) ? Text.multiLine(s) : Text.singleLine(s))
                : value);
        return cleaned;
    }

    private Template load(String name) {
        var resource = resources.getResource("classpath:templates/notifications/" + name + ".mustache");
        try (var reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return compiler.compile(reader);
        } catch (IOException e) {
            throw new UncheckedIOException("Missing notification template " + name, e);
        }
    }
}
