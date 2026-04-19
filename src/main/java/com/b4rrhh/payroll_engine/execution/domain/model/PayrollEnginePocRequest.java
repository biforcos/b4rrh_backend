package com.b4rrhh.payroll_engine.execution.domain.model;

import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Input to the payroll engine PoC execution.
 *
 * <p>All identity and period fields are required. {@code monthlySalaryAmount} must be
 * non-null and non-negative. At least one working time window is required.
 */
public final class PayrollEnginePocRequest {

    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal monthlySalaryAmount;
    private final List<WorkingTimeWindow> workingTimeWindows;

    public PayrollEnginePocRequest(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate periodStart,
            LocalDate periodEnd,
            BigDecimal monthlySalaryAmount,
            List<WorkingTimeWindow> workingTimeWindows
    ) {
        if (ruleSystemCode == null || ruleSystemCode.isBlank()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }
        if (employeeTypeCode == null || employeeTypeCode.isBlank()) {
            throw new IllegalArgumentException("employeeTypeCode is required");
        }
        if (employeeNumber == null || employeeNumber.isBlank()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }
        if (periodStart == null) {
            throw new IllegalArgumentException("periodStart is required");
        }
        if (periodEnd == null) {
            throw new IllegalArgumentException("periodEnd is required");
        }
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException(
                    "periodEnd must be >= periodStart. Got start=" + periodStart + ", end=" + periodEnd);
        }
        if (monthlySalaryAmount == null) {
            throw new IllegalArgumentException("monthlySalaryAmount is required");
        }
        if (monthlySalaryAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "monthlySalaryAmount must be >= 0. Got " + monthlySalaryAmount);
        }
        if (workingTimeWindows == null || workingTimeWindows.isEmpty()) {
            throw new IllegalArgumentException("at least one workingTimeWindow is required");
        }
        this.ruleSystemCode = ruleSystemCode;
        this.employeeTypeCode = employeeTypeCode;
        this.employeeNumber = employeeNumber;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.monthlySalaryAmount = monthlySalaryAmount;
        this.workingTimeWindows = List.copyOf(workingTimeWindows);
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getMonthlySalaryAmount() { return monthlySalaryAmount; }
    public List<WorkingTimeWindow> getWorkingTimeWindows() { return workingTimeWindows; }
}
