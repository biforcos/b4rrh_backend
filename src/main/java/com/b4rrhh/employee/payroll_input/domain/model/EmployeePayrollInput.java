package com.b4rrhh.employee.payroll_input.domain.model;

import java.math.BigDecimal;

public class EmployeePayrollInput {

    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final String conceptCode;
    private final int period;
    private BigDecimal quantity;

    private EmployeePayrollInput(String ruleSystemCode, String employeeTypeCode,
                                  String employeeNumber, String conceptCode,
                                  int period, BigDecimal quantity) {
        this.ruleSystemCode = ruleSystemCode;
        this.employeeTypeCode = employeeTypeCode;
        this.employeeNumber = employeeNumber;
        this.conceptCode = conceptCode;
        this.period = period;
        this.quantity = quantity;
    }

    public static EmployeePayrollInput create(String ruleSystemCode, String employeeTypeCode,
                                               String employeeNumber, String conceptCode,
                                               int period, BigDecimal quantity) {
        requireNonBlank(ruleSystemCode, "ruleSystemCode");
        requireNonBlank(employeeTypeCode, "employeeTypeCode");
        requireNonBlank(employeeNumber, "employeeNumber");
        requireNonBlank(conceptCode, "conceptCode");
        requireValidPeriod(period);
        requireNonNegative(quantity, "quantity");
        return new EmployeePayrollInput(ruleSystemCode, employeeTypeCode, employeeNumber,
                conceptCode, period, quantity);
    }

    public static EmployeePayrollInput rehydrate(String ruleSystemCode, String employeeTypeCode,
                                                  String employeeNumber, String conceptCode,
                                                  int period, BigDecimal quantity) {
        return new EmployeePayrollInput(ruleSystemCode, employeeTypeCode, employeeNumber,
                conceptCode, period, quantity);
    }

    public void updateQuantity(BigDecimal newQuantity) {
        requireNonNegative(newQuantity, "quantity");
        this.quantity = newQuantity;
    }

    private static void requireNonBlank(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static void requireValidPeriod(int period) {
        int year = period / 100;
        int month = period % 100;
        if (year < 2000 || year > 9999 || month < 1 || month > 12) {
            throw new IllegalArgumentException("period must be yyyyMM between 200001 and 999912, got: " + period);
        }
    }

    private static void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(field + " must be >= 0");
        }
    }

    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getConceptCode() { return conceptCode; }
    public int getPeriod() { return period; }
    public BigDecimal getQuantity() { return quantity; }
}
