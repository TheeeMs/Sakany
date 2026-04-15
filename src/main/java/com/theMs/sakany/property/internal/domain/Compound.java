package com.theMs.sakany.property.internal.domain;

import com.theMs.sakany.property.internal.domain.events.CompoundCreated;
import com.theMs.sakany.shared.domain.AggregateRoot;
import com.theMs.sakany.shared.exception.BusinessRuleException;

import java.time.Instant;
import java.util.UUID;

public class Compound extends AggregateRoot {
    private UUID id;
    private String name;
    private String address;

    private Compound(UUID id, String name, String address) {
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public static Compound create(String name, String address) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessRuleException("Compound name cannot be null or empty");
        }
        if (address == null || address.trim().isEmpty()) {
            throw new BusinessRuleException("Compound address cannot be null or empty");
        }

        UUID id = UUID.randomUUID();
        Compound compound = new Compound(id, name, address);
        compound.registerEvent(new CompoundCreated(id, name, Instant.now()));
        return compound;
    }

    public void updateInfo(String name, String address) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name;
        }
        if (address != null && !address.trim().isEmpty()) {
            this.address = address;
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
