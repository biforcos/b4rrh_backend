package com.b4rrhh.rulesystem.catalogbinding.infrastructure.persistence;

import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogKind;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@IdClass(CatalogBindingEntityId.class)
@Table(name = "resource_field_catalog_binding", schema = "rulesystem")
public class CatalogBindingEntity {

    @Id
    @Column(name = "resource_code", nullable = false, length = 80)
    private String resourceCode;

    @Id
    @Column(name = "field_code", nullable = false, length = 80)
    private String fieldCode;

    @Column(name = "rule_entity_type_code", length = 30)
    private String ruleEntityTypeCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "catalog_kind", nullable = false, length = 20)
    private CatalogKind catalogKind;

    @Column(name = "depends_on_field_code", length = 80)
    private String dependsOnFieldCode;

    @Column(name = "custom_resolver_code", length = 80)
    private String customResolverCode;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false)
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

    public String getResourceCode() {
        return resourceCode;
    }

    public void setResourceCode(String resourceCode) {
        this.resourceCode = resourceCode;
    }

    public String getFieldCode() {
        return fieldCode;
    }

    public void setFieldCode(String fieldCode) {
        this.fieldCode = fieldCode;
    }

    public String getRuleEntityTypeCode() {
        return ruleEntityTypeCode;
    }

    public void setRuleEntityTypeCode(String ruleEntityTypeCode) {
        this.ruleEntityTypeCode = ruleEntityTypeCode;
    }

    public CatalogKind getCatalogKind() {
        return catalogKind;
    }

    public void setCatalogKind(CatalogKind catalogKind) {
        this.catalogKind = catalogKind;
    }

    public String getDependsOnFieldCode() {
        return dependsOnFieldCode;
    }

    public void setDependsOnFieldCode(String dependsOnFieldCode) {
        this.dependsOnFieldCode = dependsOnFieldCode;
    }

    public String getCustomResolverCode() {
        return customResolverCode;
    }

    public void setCustomResolverCode(String customResolverCode) {
        this.customResolverCode = customResolverCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
