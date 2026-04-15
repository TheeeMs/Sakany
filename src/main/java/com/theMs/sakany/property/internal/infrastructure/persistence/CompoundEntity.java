package com.theMs.sakany.property.internal.infrastructure.persistence;

import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "compounds")
public class CompoundEntity extends BaseEntity {
    private String name;
    private String address;

    protected CompoundEntity() {
        // JPA requires no-arg constructor
    }

    public CompoundEntity(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
