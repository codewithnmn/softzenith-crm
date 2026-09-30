package com.softzenith.crm.shared.logging;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MaskTest {

    @Test
    void phonesKeepCountryCodeAndLastFourDigits() {
        assertThat(Mask.phone("+919876543210")).isEqualTo("+91******3210");
        assertThat(Mask.phone("12345")).isEqualTo("*****");
        assertThat(Mask.phone(null)).isNull();
    }

    @Test
    void emailsKeepFirstLetterAndDomain() {
        assertThat(Mask.email("asha.verma@mail.com")).isEqualTo("a***@mail.com");
        assertThat(Mask.email("not-an-email")).isEqualTo("***");
        assertThat(Mask.recipient("asha@mail.com")).isEqualTo("a***@mail.com");
        assertThat(Mask.recipient("+919876543210")).isEqualTo("+91******3210");
    }

    @Test
    void blankAndOddValuesAreHandled() {
        assertThat(Mask.phone("  ")).isEqualTo("  ");
        assertThat(Mask.email(null)).isNull();
        assertThat(Mask.email(" ")).isEqualTo(" ");
        assertThat(Mask.email("@mail.com")).isEqualTo("***");
        assertThat(Mask.recipient(null)).isNull();
    }
}
