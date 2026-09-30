package com.softzenith.crm.lead;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.UUID;

/** Lead list filters; every field is optional. */
public record LeadFilter(LeadStatus status, LeadSourceType sourceType, UUID branchId, UUID assignedTo,
                         Boolean unassigned, String q, Instant createdFrom, Instant createdTo) {

    Specification<Lead> toSpecification() {
        return (root, query, cb) -> {
            var p = new ArrayList<Predicate>();
            if (status != null) p.add(cb.equal(root.get("status"), status));
            if (sourceType != null) p.add(cb.equal(root.get("sourceType"), sourceType));
            if (branchId != null) p.add(cb.equal(root.get("branchId"), branchId));
            if (assignedTo != null) p.add(cb.equal(root.get("assignedTo"), assignedTo));
            if (Boolean.TRUE.equals(unassigned)) p.add(cb.isNull(root.get("assignedTo")));
            if (createdFrom != null) p.add(cb.greaterThanOrEqualTo(root.get("createdAt"), createdFrom));
            if (createdTo != null) p.add(cb.lessThan(root.get("createdAt"), createdTo));
            if (q != null && !q.isBlank()) {
                var like = "%" + q.trim().toLowerCase() + "%";
                var digits = q.replaceAll("\\D", "");
                var text = new ArrayList<Predicate>();
                text.add(cb.like(cb.lower(root.get("fullName")), like));
                text.add(cb.like(root.get("email"), like));
                text.add(cb.like(cb.lower(root.get("leadNumber")), like));
                if (digits.length() >= 4) text.add(cb.like(root.get("phoneE164"), "%" + digits + "%"));
                p.add(cb.or(text.toArray(Predicate[]::new)));
            }
            return cb.and(p.toArray(Predicate[]::new));
        };
    }
}
