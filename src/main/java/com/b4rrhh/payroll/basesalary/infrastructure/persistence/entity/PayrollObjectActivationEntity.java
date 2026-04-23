package com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "payroll_object_activation", schema = "payroll")
public class PayrollObjectActivationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_system_code", nullable = false, length = 10)
    private String ruleSystemCode;

    @Column(name = "owner_type_code", nullable = false, length = 50)
    private String ownerTypeCode;

    @Column(name = "owner_code", nullable = false, length = 100)
    private String ownerCode;

    @Column(name = "target_object_type_code", nullable = false, length = 50)
    private String targetObjectTypeCode;

    @Column(name = "target_object_code", nullable = false, length = 100)
    private String targetObjectCode;

    @Column(name = "active", nullable = false)
    private Boolean active;

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

    public String getTargetObjectTypeCode() {
        return targetObjectTypeCode;
    }

    public void setTargetObjectTypeCode(String targetObjectTypeCode) {
        this.targetObjectTypeCode = targetObjectTypeCode;
    }

    public String getTargetObjectCode() {
        return targetObjectCode;
    }

    public void setTargetObjectCode(String targetObjectCode) {
        this.targetObjectCode = targetObjectCode;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
