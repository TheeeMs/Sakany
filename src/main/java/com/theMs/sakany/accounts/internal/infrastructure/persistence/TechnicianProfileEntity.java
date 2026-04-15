package com.theMs.sakany.accounts.internal.infrastructure.persistence;

import com.theMs.sakany.shared.jpa.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "technician_profiles")
public class TechnicianProfileEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "specializations", columnDefinition = "text[]")
    private List<String> specializations;

    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    @Column(name = "rating")
    private Double rating;

    public TechnicianProfileEntity() {}

    public TechnicianProfileEntity(UserEntity user, List<String> specializations, boolean isAvailable, Double rating) {
        this.user = user;
        this.specializations = specializations;
        this.isAvailable = isAvailable;
        this.rating = rating;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<String> getSpecializations() {
        return specializations;
    }

    public void setSpecializations(List<String> specializations) {
        this.specializations = specializations;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}
