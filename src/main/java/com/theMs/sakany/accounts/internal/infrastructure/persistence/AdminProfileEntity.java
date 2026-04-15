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
@Table(name = "admin_profiles")
public class AdminProfileEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "scope_permissions", columnDefinition = "text[]")
    private List<String> scopePermissions;

    public AdminProfileEntity() {}

    public AdminProfileEntity(UserEntity user, List<String> scopePermissions) {
        this.user = user;
        this.scopePermissions = scopePermissions;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<String> getScopePermissions() {
        return scopePermissions;
    }

    public void setScopePermissions(List<String> scopePermissions) {
        this.scopePermissions = scopePermissions;
    }
}
