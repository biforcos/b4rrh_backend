package com.b4rrhh.payroll_engine.segment.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * Captures all data needed to calculate a single CalculationSegment for one employee.
 *
 * <p>This is a read-only value object assembled by the application layer before invoking
 * segment-level calculation logic. It carries both period-level and segment-level context
 * so that calculation rules need not re-derive them.
 *
 * <p>All date fields are inclusive. {@code daysInPeriod} and {@code daysInSegment} are
 * pre-computed inclusive day counts.
 */
public final class SegmentCalculationContext {

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be null or blank");
        }
    }

    private static void requireNonNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " must not be null");
        }
    }

    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;

    private final LocalDate periodStart;
    private final LocalDate periodEnd;
    private final LocalDate segmentStart;
    private final LocalDate segmentEnd;

    private final boolean firstSegment;
    private final boolean lastSegment;

    private final long daysInPeriod;
    private final long daysInSegment;

    private final BigDecimal workingTimePercentage;
    private final BigDecimal monthlySalaryAmount;
    private final Map<String, BigDecimal> employeeInputs;

    public SegmentCalculationContext(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate periodStart,
            LocalDate periodEnd,
            LocalDate segmentStart,
            LocalDate segmentEnd,
            boolean firstSegment,
            boolean lastSegment,
            long daysInPeriod,
            long daysInSegment,
            BigDecimal workingTimePercentage,
            BigDecimal monthlySalaryAmount,
            Map<String, BigDecimal> employeeInputs
    ) {
        requireNonBlank(ruleSystemCode, "ruleSystemCode");
        requireNonBlank(employeeTypeCode, "employeeTypeCode");
        requireNonBlank(employeeNumber, "employeeNumber");
        requireNonNull(periodStart, "periodStart");
        requireNonNull(periodEnd, "periodEnd");
        requireNonNull(segmentStart, "segmentStart");
        requireNonNull(segmentEnd, "segmentEnd");
        if (periodEnd.isBefore(periodStart)) {
            throw new IllegalArgumentException("periodEnd must not be before periodStart");
        }
        if (segmentEnd.isBefore(segmentStart)) {
            throw new IllegalArgumentException("segmentEnd must not be before segmentStart");
        }
        if (segmentStart.isBefore(periodStart) || segmentEnd.isAfter(periodEnd)) {
            throw new IllegalArgumentException("segment [" + segmentStart + ", " + segmentEnd +
                    "] must be contained within period [" + periodStart + ", " + periodEnd + "]");
        }
        if (daysInPeriod <= 0) {
            throw new IllegalArgumentException("daysInPeriod must be > 0, got: " + daysInPeriod);
        }
        if (daysInSegment <= 0) {
            throw new IllegalArgumentException("daysInSegment must be > 0, got: " + daysInSegment);
        }
        requireNonNull(workingTimePercentage, "workingTimePercentage");
        requireNonNull(monthlySalaryAmount, "monthlySalaryAmount");
        requireNonNull(employeeInputs, "employeeInputs");
        this.ruleSystemCode = ruleSystemCode;
        this.employeeTypeCode = employeeTypeCode;
        this.employeeNumber = employeeNumber;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.segmentStart = segmentStart;
        this.segmentEnd = segmentEnd;
        this.firstSegment = firstSegment;
        this.lastSegment = lastSegment;
        this.daysInPeriod = daysInPeriod;
        this.daysInSegment = daysInSegment;
        this.workingTimePercentage = workingTimePercentage;
        this.monthlySalaryAmount = monthlySalaryAmount;
        this.employeeInputs = employeeInputs;
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public LocalDate getPeriodStart() { return periodStart; }
    public LocalDate getPeriodEnd() { return periodEnd; }
    public LocalDate getSegmentStart() { return segmentStart; }
    public LocalDate getSegmentEnd() { return segmentEnd; }
    public boolean isFirstSegment() { return firstSegment; }
    public boolean isLastSegment() { return lastSegment; }
    public long getDaysInPeriod() { return daysInPeriod; }
    public long getDaysInSegment() { return daysInSegment; }
    public BigDecimal getWorkingTimePercentage() { return workingTimePercentage; }
    public BigDecimal getMonthlySalaryAmount() { return monthlySalaryAmount; }
    public Map<String, BigDecimal> getEmployeeInputs() { return employeeInputs; }
}
