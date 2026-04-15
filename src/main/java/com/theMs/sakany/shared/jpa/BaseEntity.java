package com.theMs.sakany.shared.jpa;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

import org.springframework.data.domain.Persistable;

@MappedSuperclass
public abstract class BaseEntity implements Persistable<UUID> {
    @Id
    private UUID id;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;

    @Override
    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean isNew() {
        // If createdAt is null, this is a newly created entity that hasn't been saved yet.
        return this.createdAt == null;
    }
}
