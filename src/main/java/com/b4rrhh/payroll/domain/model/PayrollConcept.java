package com.b4rrhh.payroll.domain.model;

import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;

import java.math.BigDecimal;

public class PayrollConcept {

    private final Integer lineNumber;
    private final String conceptCode;
    private final String conceptLabel;
    private final BigDecimal amount;
    private final BigDecimal quantity;
    private final BigDecimal rate;
    private final String conceptNatureCode;
    private final String originPeriodCode;
    private final Integer displayOrder;

    public PayrollConcept(
            Integer lineNumber,
            String conceptCode,
            String conceptLabel,
            BigDecimal amount,
            BigDecimal quantity,
            BigDecimal rate,
            String conceptNatureCode,
            String originPeriodCode,
            Integer displayOrder
    ) {
        this.lineNumber = requirePositive(lineNumber, "lineNumber");
        this.conceptCode = requireCode(conceptCode, "conceptCode", 30);
        this.conceptLabel = requireText(conceptLabel, "conceptLabel", 200);
        this.amount = requireAmount(amount, "amount");
        this.quantity = normalizeDecimal(quantity, "quantity");
        this.rate = normalizeDecimal(rate, "rate");
        this.conceptNatureCode = requireCode(conceptNatureCode, "conceptNatureCode", 30);
        this.originPeriodCode = normalizeOptional(originPeriodCode, "originPeriodCode", 30);
        this.displayOrder = requirePositive(displayOrder, "displayOrder");
    }

    private static Integer requirePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new InvalidPayrollArgumentException(fieldName + " must be a positive integer");
        }
        return value;
    }

    private static String requireCode(String value, String fieldName, int maxLength) {
        String normalized = normalizeRequired(value, fieldName, maxLength);
        return normalized.toUpperCase();
    }

    private static String requireText(String value, String fieldName, int maxLength) {
        return normalizeRequired(value, fieldName, maxLength);
    }

    private static String normalizeRequired(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPayrollArgumentException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new InvalidPayrollArgumentException(fieldName + " exceeds max length " + maxLength);
        }
        return normalized;
    }

    private static String normalizeOptional(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new InvalidPayrollArgumentException(fieldName + " exceeds max length " + maxLength);
        }
        return normalized.toUpperCase();
    }

    private static BigDecimal requireAmount(BigDecimal value, String fieldName) {
        BigDecimal normalized = normalizeDecimal(value, fieldName);
        if (normalized == null) {
            throw new InvalidPayrollArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private static BigDecimal normalizeDecimal(BigDecimal value, String fieldName) {
        if (value == null) {
            return null;
        }
        if (value.scale() > 6) {
            throw new InvalidPayrollArgumentException(fieldName + " scale exceeds 6 decimals");
        }
        return value.stripTrailingZeros();
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getConceptCode() {
        return conceptCode;
    }

    public String getConceptLabel() {
        return conceptLabel;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public String getConceptNatureCode() {
        return conceptNatureCode;
    }

    public String getOriginPeriodCode() {
        return originPeriodCode;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }
}