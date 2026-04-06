package com.b4rrhh.rulesystem.workcenter.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "work_center_contact",
        schema = "rulesystem",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_work_center_contact_number",
                columnNames = {"work_center_rule_entity_id", "contact_number"}
        )
)
public class WorkCenterContactEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "work_center_rule_entity_id", nullable = false)
    private Long workCenterRuleEntityId;

    @Column(name = "contact_number", nullable = false)
    private Integer contactNumber;

    @Column(name = "contact_type_code", nullable = false, length = 30)
    private String contactTypeCode;

    @Column(name = "contact_value", nullable = false, length = 300)
    private String contactValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getWorkCenterRuleEntityId() {
        return workCenterRuleEntityId;
    }

    public void setWorkCenterRuleEntityId(Long workCenterRuleEntityId) {
        this.workCenterRuleEntityId = workCenterRuleEntityId;
    }

    public Integer getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(Integer contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactTypeCode() {
        return contactTypeCode;
    }

    public void setContactTypeCode(String contactTypeCode) {
        this.contactTypeCode = contactTypeCode;
    }

    public String getContactValue() {
        return contactValue;
    }

    public void setContactValue(String contactValue) {
        this.contactValue = contactValue;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}