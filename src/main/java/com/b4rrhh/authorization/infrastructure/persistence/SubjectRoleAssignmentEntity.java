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
@Table(name = "subject_role_assignment", schema = "authz")
@IdClass(SubjectRoleAssignmentEntity.Pk.class)
public class SubjectRoleAssignmentEntity {

    @Id
    @Column(name = "subject_code", nullable = false, length = 100)
    private String subjectCode;

    @Id
    @Column(name = "role_code", nullable = false, length = 50)
    private String roleCode;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "assignment_origin", nullable = false, length = 20)
    private String assignmentOrigin;

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

    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getAssignmentOrigin() { return assignmentOrigin; }
    public void setAssignmentOrigin(String assignmentOrigin) { this.assignmentOrigin = assignmentOrigin; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static class Pk implements Serializable {
        private String subjectCode;
        private String roleCode;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Pk pk)) return false;
            return Objects.equals(subjectCode, pk.subjectCode) && Objects.equals(roleCode, pk.roleCode);
        }

        @Override
        public int hashCode() {
            return Objects.hash(subjectCode, roleCode);
        }
    }
}