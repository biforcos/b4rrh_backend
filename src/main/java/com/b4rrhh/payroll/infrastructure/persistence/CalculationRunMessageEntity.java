package com.b4rrhh.payroll.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "calculation_run_message", schema = "payroll")
public class CalculationRunMessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "run_id", nullable = false)
    private CalculationRunEntity calculationRun;

    @Column(name = "message_code", nullable = false, length = 50)
    private String messageCode;

    @Column(name = "severity_code", nullable = false, length = 20)
    private String severityCode;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json", columnDefinition = "json")
    private String detailsJson;

    @Column(name = "rule_system_code", length = 5)
    private String ruleSystemCode;

    @Column(name = "employee_type_code", length = 30)
    private String employeeTypeCode;

    @Column(name = "employee_number", length = 15)
    private String employeeNumber;

    @Column(name = "payroll_period_code", length = 30)
    private String payrollPeriodCode;

    @Column(name = "payroll_type_code", length = 30)
    private String payrollTypeCode;

    @Column(name = "presence_number")
    private Integer presenceNumber;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public CalculationRunEntity getCalculationRun() { return calculationRun; }
    public void setCalculationRun(CalculationRunEntity calculationRun) { this.calculationRun = calculationRun; }
    public String getMessageCode() { return messageCode; }
    public void setMessageCode(String messageCode) { this.messageCode = messageCode; }
    public String getSeverityCode() { return severityCode; }
    public void setSeverityCode(String severityCode) { this.severityCode = severityCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}