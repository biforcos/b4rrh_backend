package com.b4rrhh.authorization.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "secured_resource", schema = "authz")
public class SecuredResourceEntity {

    @Id
    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "parent_code", length = 100)
    private String parentCode;

    @Column(name = "bounded_context_code", nullable = false, length = 50)
    private String boundedContextCode;

    @Column(name = "resource_kind", nullable = false, length = 50)
    private String resourceKind;

    @Column(name = "resource_family_code", nullable = false, length = 50)
    private String resourceFamilyCode;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getParentCode() { return parentCode; }
    public void setParentCode(String parentCode) { this.parentCode = parentCode; }

    public String getBoundedContextCode() { return boundedContextCode; }
    public void setBoundedContextCode(String boundedContextCode) { this.boundedContextCode = boundedContextCode; }

    public String getResourceKind() { return resourceKind; }
    public void setResourceKind(String resourceKind) { this.resourceKind = resourceKind; }

    public String getResourceFamilyCode() { return resourceFamilyCode; }
    public void setResourceFamilyCode(String resourceFamilyCode) { this.resourceFamilyCode = resourceFamilyCode; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
