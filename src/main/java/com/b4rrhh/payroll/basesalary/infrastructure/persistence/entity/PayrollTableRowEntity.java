package com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * JPA entity for payroll.payroll_table_row table.
 * Persistence-only; do not expose outside infrastructure layer.
 */
@Entity
@Table(name = "payroll_table_row", schema = "payroll")
public class PayrollTableRowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_system_code", nullable = false, length = 10)
    private String ruleSystemCode;

    @Column(name = "table_code", nullable = false, length = 100)
    private String tableCode;

    @Column(name = "search_code", nullable = false, length = 100)
    private String searchCode;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "monthly_value", precision = 10, scale = 2)
    private BigDecimal monthlyValue;

    @Column(name = "annual_value", precision = 10, scale = 2)
    private BigDecimal annualValue;

    @Column(name = "daily_value", precision = 10, scale = 2)
    private BigDecimal dailyValue;

    @Column(name = "hourly_value", precision = 10, scale = 2)
    private BigDecimal hourlyValue;

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

    public String getTableCode() {
        return tableCode;
    }

    public void setTableCode(String tableCode) {
        this.tableCode = tableCode;
    }

    public String getSearchCode() {
        return searchCode;
    }

    public void setSearchCode(String searchCode) {
        this.searchCode = searchCode;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMonthlyValue() {
        return monthlyValue;
    }

    public void setMonthlyValue(BigDecimal monthlyValue) {
        this.monthlyValue = monthlyValue;
    }

    public BigDecimal getAnnualValue() {
        return annualValue;
    }

    public void setAnnualValue(BigDecimal annualValue) {
        this.annualValue = annualValue;
    }

    public BigDecimal getDailyValue() {
        return dailyValue;
    }

    public void setDailyValue(BigDecimal dailyValue) {
        this.dailyValue = dailyValue;
    }

    public BigDecimal getHourlyValue() {
        return hourlyValue;
    }

    public void setHourlyValue(BigDecimal hourlyValue) {
        this.hourlyValue = hourlyValue;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
