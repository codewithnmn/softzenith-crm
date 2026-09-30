package com.softzenith.crm.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirstNameTest {

    @Test
    void greetsByFirstNameOnlyWhenItLooksLikeAName() {
        assertThat(LeadNotifications.firstName("Asha Verma")).isEqualTo("Asha");
        assertThat(LeadNotifications.firstName("Ana-Maria D'Souza")).isEqualTo("Ana-Maria");
        assertThat(LeadNotifications.firstName("Rāj Kumār")).isEqualTo("Rāj");
        assertThat(LeadNotifications.firstName("www.evil.example pay now")).isEqualTo("there");
        assertThat(LeadNotifications.firstName("")).isEqualTo("there");
    }
}
