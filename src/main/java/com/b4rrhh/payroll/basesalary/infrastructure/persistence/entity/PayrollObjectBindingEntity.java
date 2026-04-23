package com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity;

import jakarta.persistence.*;

/**
 * JPA entity for payroll.payroll_object_binding table.
 * Persistence-only; do not expose outside infrastructure layer.
 */
@Entity
@Table(name = "payroll_object_binding", schema = "payroll")
public class PayrollObjectBindingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_system_code", nullable = false, length = 10)
    private String ruleSystemCode;

    @Column(name = "owner_type_code", nullable = false, length = 50)
    private String ownerTypeCode;

    @Column(name = "owner_code", nullable = false, length = 100)
    private String ownerCode;

    @Column(name = "binding_role_code", nullable = false, length = 50)
    private String bindingRoleCode;

    @Column(name = "bound_object_type_code", nullable = false, length = 50)
    private String boundObjectTypeCode;

    @Column(name = "bound_object_code", nullable = false, length = 100)
    private String boundObjectCode;

    @Column(name = "active", nullable = false)
    private Boolean active;

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleSystemCode() {
        return ruleSystemCode;
    }

    public void setRuleSystemCode(String ruleSystemCode) {
        this.ruleSystemCode = ruleSystemCode;
    }

    public String getOwnerTypeCode() {
        return ownerTypeCode;
    }

    public void setOwnerTypeCode(String ownerTypeCode) {
        this.ownerTypeCode = ownerTypeCode;
    }

    public String getOwnerCode() {
        return ownerCode;
    }

    public void setOwnerCode(String ownerCode) {
        this.ownerCode = ownerCode;
    }

    public String getBindingRoleCode() {
        return bindingRoleCode;
    }

    public void setBindingRoleCode(String bindingRoleCode) {
        this.bindingRoleCode = bindingRoleCode;
    }

    public String getBoundObjectTypeCode() {
        return boundObjectTypeCode;
    }

    public void setBoundObjectTypeCode(String boundObjectTypeCode) {
        this.boundObjectTypeCode = boundObjectTypeCode;
    }

    public String getBoundObjectCode() {
        return boundObjectCode;
    }

    public void setBoundObjectCode(String boundObjectCode) {
        this.boundObjectCode = boundObjectCode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
