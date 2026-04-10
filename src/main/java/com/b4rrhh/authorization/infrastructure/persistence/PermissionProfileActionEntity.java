package com.b4rrhh.authorization.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "permission_profile_action", schema = "authz")
@IdClass(PermissionProfileActionEntity.Pk.class)
public class PermissionProfileActionEntity {

    @Id
    @Column(name = "permission_profile_code", nullable = false, length = 50)
    private String permissionProfileCode;

    @Id
    @Column(name = "action_code", nullable = false, length = 50)
    private String actionCode;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }

    public String getPermissionProfileCode() { return permissionProfileCode; }
    public void setPermissionProfileCode(String permissionProfileCode) { this.permissionProfileCode = permissionProfileCode; }

    public String getActionCode() { return actionCode; }
    public void setActionCode(String actionCode) { this.actionCode = actionCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public static class Pk implements Serializable {
        private String permissionProfileCode;
        private String actionCode;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk pk)) return false;
            return Objects.equals(permissionProfileCode, pk.permissionProfileCode)
                    && Objects.equals(actionCode, pk.actionCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(permissionProfileCode, actionCode);
        }
    }
}
