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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "payroll_warning", schema = "payroll")
public class PayrollWarningEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_id", nullable = false)
    private PayrollEntity payroll;

    @Column(name = "warning_code", nullable = false, length = 50)
    private String warningCode;

    @Column(name = "severity_code", nullable = false, length = 20)
    private String severityCode;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json", columnDefinition = "json")
    private String detailsJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PayrollEntity getPayroll() { return payroll; }
    public void setPayroll(PayrollEntity payroll) { this.payroll = payroll; }
    public String getWarningCode() { return warningCode; }
    public void setWarningCode(String warningCode) { this.warningCode = warningCode; }
    public String getSeverityCode() { return severityCode; }
    public void setSeverityCode(String severityCode) { this.severityCode = severityCode; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getDetailsJson() { return detailsJson; }
    public void setDetailsJson(String detailsJson) { this.detailsJson = detailsJson; }
}