package com.softzenith.crm.shared.text;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextTest {

    @Test
    void singleLineRemovesEveryLineBreakAndControlCharacter() {
        assertThat(Text.singleLine("  Asha\r\nFAKE LOG LINE\u0000 x  ")).isEqualTo("Asha FAKE LOG LINE x");
        assertThat(Text.singleLine(" \t ")).isNull();
        assertThat(Text.singleLine(null)).isNull();
    }

    @Test
    void multiLineKeepsLineBreaksOnly() {
        assertThat(Text.multiLine("Hello\r\nworld\u0007\n\tok")).isEqualTo("Hello\nworld\n\tok");
        assertThat(Text.multiLine("‎")).isNull();
    }
}
