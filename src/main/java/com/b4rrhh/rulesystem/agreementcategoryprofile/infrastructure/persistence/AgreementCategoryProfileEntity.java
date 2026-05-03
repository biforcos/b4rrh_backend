package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.persistence;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "agreement_category_profile",
        schema = "rulesystem",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_agreement_category_profile",
                columnNames = "agreement_category_rule_entity_id"
        )
)
public class AgreementCategoryProfileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agreement_category_rule_entity_id", nullable = false)
    private Long agreementCategoryRuleEntityId;

    @Column(name = "grupo_cotizacion_code", nullable = false, length = 2)
    private String grupoCotizacionCode;

    @Column(name = "tipo_nomina", nullable = false, length = 10)
    private String tipoNomina;

    @Column(name = "created_at", nullable = false, updatable = false)
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
    void preUpdate() { updatedAt = LocalDateTime.now(); }

    public Long getId()                                { return id; }
    public void setId(Long id)                         { this.id = id; }
    public Long getAgreementCategoryRuleEntityId()     { return agreementCategoryRuleEntityId; }
    public void setAgreementCategoryRuleEntityId(Long v) { this.agreementCategoryRuleEntityId = v; }
    public String getGrupoCotizacionCode()             { return grupoCotizacionCode; }
    public void setGrupoCotizacionCode(String v)       { this.grupoCotizacionCode = v; }
    public String getTipoNomina()                      { return tipoNomina; }
    public void setTipoNomina(String v)                { this.tipoNomina = v; }
    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void setCreatedAt(LocalDateTime v)          { this.createdAt = v; }
    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void setUpdatedAt(LocalDateTime v)          { this.updatedAt = v; }
}
