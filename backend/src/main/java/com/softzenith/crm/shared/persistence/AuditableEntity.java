package com.softzenith.crm.shared.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import org.hibernate.Hibernate;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

/** UUIDv7 id, audit columns and optimistic locking shared by every table. */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class AuditableEntity {

    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    private UUID id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @CreatedBy
    @Column(updatable = false)
    private UUID createdBy;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @LastModifiedBy
    private UUID updatedBy;

    @Version
    private long version;

    public UUID getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getUpdatedAt() { return updatedAt; }
    public UUID getUpdatedBy() { return updatedBy; }
    public long getVersion() { return version; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        return id != null && id.equals(((AuditableEntity) o).id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
