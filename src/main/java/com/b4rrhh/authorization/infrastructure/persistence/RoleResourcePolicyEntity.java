package com.b4rrhh.authorization.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "role_resource_policy", schema = "authz")
@IdClass(RoleResourcePolicyEntity.Pk.class)
public class RoleResourcePolicyEntity {

    @Id
    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    @Id
    @Column(name = "resource_code", nullable = false, length = 100)
    private String resourceCode;

    @Column(name = "permission_profile_code", nullable = false, length = 50)
    private String permissionProfileCode;

    @Column(name = "effect", nullable = false, length = 20)
    private String effect;

    @Column(name = "propagation_mode", nullable = false, length = 50)
    private String propagationMode;

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

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getResourceCode() { return resourceCode; }
    public void setResourceCode(String resourceCode) { this.resourceCode = resourceCode; }

    public String getPermissionProfileCode() { return permissionProfileCode; }
    public void setPermissionProfileCode(String permissionProfileCode) { this.permissionProfileCode = permissionProfileCode; }

    public String getEffect() { return effect; }
    public void setEffect(String effect) { this.effect = effect; }

    public String getPropagationMode() { return propagationMode; }
    public void setPropagationMode(String propagationMode) { this.propagationMode = propagationMode; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class Pk implements Serializable {
        private String roleCode;
        private String resourceCode;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk pk)) return false;
            return Objects.equals(roleCode, pk.roleCode) && Objects.equals(resourceCode, pk.resourceCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleCode, resourceCode);
        }
    }
}
