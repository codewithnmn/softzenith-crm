package com.softzenith.crm.shared.text;

import java.util.regex.Pattern;

/**
 * Cleans free text from outside (public forms, source adapters) before it is stored, logged or put into a
 * message: control characters could forge log lines, break an email subject or smuggle text into a message.
 */
public final class Text {

    /** Control, format and line/paragraph separator characters (tab/newline handled separately). */
    private static final Pattern CONTROL = Pattern.compile("[\\p{Cc}\\p{Cf}\\u2028\\u2029]");
    private static final Pattern SPACES = Pattern.compile("[ \t]{2,}");

    private Text() {
    }

    /** One line: every control character becomes a space; trimmed; blank becomes null. */
    public static String singleLine(String s) {
        if (s == null) {
            return null;
        }
        var cleaned = SPACES.matcher(CONTROL.matcher(s).replaceAll(" ")).replaceAll(" ").strip();
        return cleaned.isEmpty() ? null : cleaned;
    }

    /** Keeps line breaks (normalised to \n) and tabs; drops every other control character; blank becomes null. */
    public static String multiLine(String s) {
        if (s == null) {
            return null;
        }
        var normalised = s.replace("\r\n", "\n").replace('\r', '\n');
        var sb = new StringBuilder(normalised.length());
        normalised.codePoints().forEach(c -> {
            if (c == '\n' || c == '\t' || !CONTROL.matcher(Character.toString(c)).matches()) {
                sb.appendCodePoint(c);
            }
        });
        var cleaned = sb.toString().strip();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
