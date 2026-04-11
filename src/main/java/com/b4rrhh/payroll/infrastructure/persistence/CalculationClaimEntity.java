package com.b4rrhh.payroll.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "calculation_claim", schema = "payroll")
public class CalculationClaimEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private CalculationRunEntity calculationRun;

    @Column(name = "rule_system_code", nullable = false, length = 5)
    private String ruleSystemCode;

    @Column(name = "employee_type_code", nullable = false, length = 30)
    private String employeeTypeCode;

    @Column(name = "employee_number", nullable = false, length = 15)
    private String employeeNumber;

    @Column(name = "payroll_period_code", nullable = false, length = 30)
    private String payrollPeriodCode;

    @Column(name = "payroll_type_code", nullable = false, length = 30)
    private String payrollTypeCode;

    @Column(name = "presence_number", nullable = false)
    private Integer presenceNumber;

    @Column(name = "claimed_at", nullable = false)
    private LocalDateTime claimedAt;

    @Column(name = "claimed_by", length = 100)
    private String claimedBy;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CalculationRunEntity getCalculationRun() { return calculationRun; }
    public void setCalculationRun(CalculationRunEntity calculationRun) { this.calculationRun = calculationRun; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public void setRuleSystemCode(String ruleSystemCode) { this.ruleSystemCode = ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public void setEmployeeTypeCode(String employeeTypeCode) { this.employeeTypeCode = employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getPayrollPeriodCode() { return payrollPeriodCode; }
    public void setPayrollPeriodCode(String payrollPeriodCode) { this.payrollPeriodCode = payrollPeriodCode; }
    public String getPayrollTypeCode() { return payrollTypeCode; }
    public void setPayrollTypeCode(String payrollTypeCode) { this.payrollTypeCode = payrollTypeCode; }
    public Integer getPresenceNumber() { return presenceNumber; }
    public void setPresenceNumber(Integer presenceNumber) { this.presenceNumber = presenceNumber; }
    public LocalDateTime getClaimedAt() { return claimedAt; }
    public void setClaimedAt(LocalDateTime claimedAt) { this.claimedAt = claimedAt; }
    public String getClaimedBy() { return claimedBy; }
    public void setClaimedBy(String claimedBy) { this.claimedBy = claimedBy; }
}