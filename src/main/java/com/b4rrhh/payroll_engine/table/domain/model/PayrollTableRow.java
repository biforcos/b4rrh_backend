package com.b4rrhh.payroll_engine.table.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PayrollTableRow {

    private final Long id;
    private final String ruleSystemCode;
    private final String tableCode;
    private final String searchCode;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final BigDecimal monthlyValue;
    private final BigDecimal annualValue;
    private final BigDecimal dailyValue;
    private final BigDecimal hourlyValue;
    private final boolean active;

    public PayrollTableRow(
            Long id,
            String ruleSystemCode,
            String tableCode,
            String searchCode,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal monthlyValue,
            BigDecimal annualValue,
            BigDecimal dailyValue,
            BigDecimal hourlyValue,
            boolean active
    ) {
        this.id = id;
        this.ruleSystemCode = ruleSystemCode;
        this.tableCode = tableCode;
        this.searchCode = searchCode;
        this.startDate = startDate;
        this.endDate = endDate;
        this.monthlyValue = monthlyValue;
        this.annualValue = annualValue;
        this.dailyValue = dailyValue;
        this.hourlyValue = hourlyValue;
        this.active = active;
    }

    public Long getId() { return id; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getTableCode() { return tableCode; }
    public String getSearchCode() { return searchCode; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getMonthlyValue() { return monthlyValue; }
    public BigDecimal getAnnualValue() { return annualValue; }
    public BigDecimal getDailyValue() { return dailyValue; }
    public BigDecimal getHourlyValue() { return hourlyValue; }
    public boolean isActive() { return active; }
}
