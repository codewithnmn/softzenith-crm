package com.softzenith.crm.lead;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface LeadRepository extends JpaRepository<Lead, UUID>, JpaSpecificationExecutor<Lead> {

    /** The open lead for this person, if any (same rule as the leads_open_person_uq index). */
    @Query("""
            select l from Lead l
            where l.phoneE164 = :phone and l.status <> com.softzenith.crm.lead.LeadStatus.CLOSED
              and ((:email is null and l.email is null) or l.email = :email)""")
    Optional<Lead> findOpenByPerson(String phone, String email);

    /**
     * Serialises intake for one person (key = tenant + phone) until the transaction ends, so two simultaneous
     * enquiries (double submit, retry) become one lead plus a repeat enquiry instead of a unique-index failure.
     */
    @Query(value = "select 1 from pg_advisory_xact_lock(hashtextextended(:key, 0))", nativeQuery = true)
    Integer lockPerson(String key);

    /**
     * Atomic bump that deliberately skips the optimistic-lock version: a repeat enquiry must never fail
     * because a staff member happens to be editing the same lead.
     */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Lead l set l.lastEnquiryAt = :at, l.enquiryCount = l.enquiryCount + 1 where l.id = :id")
    void recordRepeatEnquiry(UUID id, Instant at);

    // Dashboard counts for leads created in [from, to). Rows are {key, count}.

    @Query("select l.status, count(l) from Lead l where l.createdAt >= :from and l.createdAt < :to group by l.status")
    List<Object[]> countByStatus(Instant from, Instant to);

    @Query("select l.sourceType, count(l) from Lead l where l.createdAt >= :from and l.createdAt < :to group by l.sourceType")
    List<Object[]> countBySource(Instant from, Instant to);

    @Query("""
            select l.assignedTo, count(l) from Lead l
            where l.createdAt >= :from and l.createdAt < :to and l.assignedTo is not null
            group by l.assignedTo""")
    List<Object[]> countByAssignee(Instant from, Instant to);

    @Query("""
            select count(l) from Lead l
            where l.createdAt >= :from and l.createdAt < :to
              and l.assignedTo is null and l.status <> com.softzenith.crm.lead.LeadStatus.CLOSED""")
    long countOpenUnassigned(Instant from, Instant to);
}
