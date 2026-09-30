package com.softzenith.crm.lead;

import com.softzenith.crm.shared.web.ConflictException;
import com.softzenith.crm.shared.web.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Lead status and assignment rules, without Spring or a database. */
class LeadTest {

    private final UUID counsellor = UUID.randomUUID();
    private final UUID receptionist = UUID.randomUUID();

    @Test
    void aNewLeadIsNewAndUnassigned() {
        var lead = lead();
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(lead.getAssignedTo()).isNull();
        assertThat(lead.getEnquiryCount()).isEqualTo(1);
    }

    @Test
    void assigningSetsAssignedAndWhoDidIt() {
        var lead = lead();
        lead.assignTo(counsellor, receptionist);
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.ASSIGNED);
        assertThat(lead.getAssignedTo()).isEqualTo(counsellor);
        assertThat(lead.getAssignedBy()).isEqualTo(receptionist);
        assertThat(lead.getAssignedAt()).isNotNull();
    }

    @Test
    void contactingAnAssignedLeadKeepsTheCounsellor() {
        var lead = lead();
        lead.assignTo(counsellor, receptionist);
        lead.changeStatus(LeadStatus.CONTACTED, null);
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.CONTACTED);
        assertThat(lead.getAssignedTo()).isEqualTo(counsellor);
    }

    @Test
    void closingNeedsAReasonAndRecordsIt() {
        var lead = lead();
        assertThatThrownBy(() -> lead.changeStatus(LeadStatus.CLOSED, " ")).isInstanceOf(InvalidInputException.class);
        lead.changeStatus(LeadStatus.CLOSED, "  Not interested\n");
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.CLOSED);
        assertThat(lead.getClosedReason()).isEqualTo("Not interested");
        assertThat(lead.getClosedAt()).isNotNull();
    }

    @Test
    void reopeningClearsTheCloseAndReturnsTheLeadToTheUnassignedQueue() {
        var lead = lead();
        lead.assignTo(counsellor, receptionist);
        lead.changeStatus(LeadStatus.CLOSED, "No response");
        lead.changeStatus(LeadStatus.NEW, null);

        assertThat(lead.getStatus()).isEqualTo(LeadStatus.NEW);
        assertThat(lead.getClosedReason()).isNull();
        assertThat(lead.getClosedAt()).isNull();
        assertThat(lead.getAssignedTo()).isNull();
        assertThat(lead.getAssignedBy()).isNull();
        assertThat(lead.getAssignedAt()).isNull();
    }

    @Test
    void reopeningToContactedAlsoUnassigns() {
        var lead = lead();
        lead.assignTo(counsellor, receptionist);
        lead.changeStatus(LeadStatus.CLOSED, "No response");
        lead.changeStatus(LeadStatus.CONTACTED, null);
        assertThat(lead.getAssignedTo()).isNull();
    }

    @Test
    void assignedIsOnlyReachedByAssigning() {
        var lead = lead();
        assertThatThrownBy(() -> lead.changeStatus(LeadStatus.ASSIGNED, null)).isInstanceOf(ConflictException.class);
    }

    @Test
    void changingToTheCurrentStatusIsAConflict() {
        var lead = lead();
        assertThatThrownBy(() -> lead.changeStatus(LeadStatus.NEW, null)).isInstanceOf(ConflictException.class);
    }

    @Test
    void aClosedLeadCannotBeAssigned() {
        var lead = lead();
        lead.changeStatus(LeadStatus.CLOSED, "Duplicate");
        assertThatThrownBy(() -> lead.assignTo(counsellor, receptionist)).isInstanceOf(ConflictException.class);
    }

    @Test
    void emailIsStoredLowerCaseAndBlankAsNull() {
        assertThat(Lead.normaliseEmail("  Asha@Mail.Test ")).isEqualTo("asha@mail.test");
        assertThat(Lead.normaliseEmail("  ")).isNull();
        assertThat(Lead.normaliseEmail(null)).isNull();
    }

    private static Lead lead() {
        var d = new NewLead("Asha Verma", "9876543210", "Asha@Mail.test", null, null, null, null,
                LeadSourceType.WEBSITE_FORM, null, null, null, null, null, null);
        return new Lead("LD-000001", d, "+919876543210");
    }
}
