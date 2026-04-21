package com.b4rrhh.payroll_engine.planning.application.service;

import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Input to the eligible payroll execution use case.
 *
 * <p>This request bridges:
 * <ul>
 *   <li>the <strong>eligibility context</strong> (ruleSystemCode, companyCode, agreementCode,
 *       employeeTypeCode) — used to resolve which payroll concepts apply to this employee</li>
 *   <li>the <strong>segment execution input</strong> (period, salary, working time windows) —
 *       used to perform the per-segment calculation</li>
 * </ul>
 *
 * <p>{@code companyCode} and {@code agreementCode} are nullable: null means unknown/unspecified
 * for eligibility purposes (only wildcard assignments match).
 *
 * <p>In this first iteration, {@code referenceDate} is NOT an explicit request field.
 * The executor derives it as {@code periodStart}. Eligibility resolution and feed-relation
 * filtering are both evaluated against that derived date.
 *
 * <p>This is an intentional convention for the initial end-to-end flow. A future iteration
 * may promote {@code referenceDate} to an explicit input if business needs require it.
 */
public final class EligiblePayrollExecutionRequest {

    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final String companyCode;
    private final String agreementCode;
    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final BigDecimal monthlySalaryAmount;
    private final List<WorkingTimeWindow> workingTimeWindows;

    public EligiblePayrollExecutionRequest(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String companyCode,
            String agreementCode,
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
        this.companyCode = companyCode;
        this.agreementCode = agreementCode;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.monthlySalaryAmount = monthlySalaryAmount;
        this.workingTimeWindows = List.copyOf(workingTimeWindows);
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getCompanyCode() { return companyCode; }
    public String getAgreementCode() { return agreementCode; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public BigDecimal getMonthlySalaryAmount() { return monthlySalaryAmount; }
    public List<WorkingTimeWindow> getWorkingTimeWindows() { return workingTimeWindows; }
}
