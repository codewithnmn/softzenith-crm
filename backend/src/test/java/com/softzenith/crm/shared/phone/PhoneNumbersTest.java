package com.softzenith.crm.shared.phone;

import com.softzenith.crm.shared.web.InvalidInputException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PhoneNumbersTest {

    @ParameterizedTest
    @ValueSource(strings = {"9876543210", "+91 98765 43210", "098765-43210", "+919876543210", "0091 9876543210"})
    void normalisesIndianFormatsToE164(String raw) {
        assertThat(PhoneNumbers.toE164(raw, "IN")).isEqualTo("+919876543210");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "  ", "12345", "abcdefghij"})
    void rejectsInvalidNumbers(String raw) {
        assertThatThrownBy(() -> PhoneNumbers.toE164(raw, "IN")).isInstanceOf(InvalidInputException.class);
    }
}
