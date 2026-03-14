package com.b4rrhh.employee.identifier.domain.model;

import com.b4rrhh.employee.identifier.domain.exception.IdentifierValueInvalidException;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Identifier {

    private final Long id;
    private final Long employeeId;
    private final String identifierTypeCode;
    private final String identifierValue;
    private final String issuingCountryCode;
    private final LocalDate expirationDate;
    private final boolean isPrimary;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Identifier(
            Long id,
            Long employeeId,
            String identifierTypeCode,
            String identifierValue,
            String issuingCountryCode,
            LocalDate expirationDate,
            Boolean isPrimary,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.identifierTypeCode = normalizeRequiredCode("identifierTypeCode", identifierTypeCode);
        this.identifierValue = normalizeRequiredValue(identifierValue);
        this.issuingCountryCode = normalizeOptionalCode(issuingCountryCode);
        this.expirationDate = expirationDate;
        this.isPrimary = normalizePrimaryFlag(isPrimary);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Identifier update(
            String newIdentifierValue,
            String newIssuingCountryCode,
            LocalDate newExpirationDate,
            Boolean newIsPrimary
    ) {
        return new Identifier(
                id,
                employeeId,
                identifierTypeCode,
                newIdentifierValue,
                newIssuingCountryCode,
                newExpirationDate,
                newIsPrimary,
                createdAt,
                updatedAt
        );
    }

    private String normalizeRequiredCode(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IdentifierValueInvalidException(fieldName + " is required");
        }

        return value.trim().toUpperCase();
    }

    private String normalizeRequiredValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IdentifierValueInvalidException("identifierValue is required");
        }

        return value.trim();
    }

    private String normalizeOptionalCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim().toUpperCase();
    }

    private boolean normalizePrimaryFlag(Boolean value) {
        return value != null && value;
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getIdentifierTypeCode() {
        return identifierTypeCode;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public String getIssuingCountryCode() {
        return issuingCountryCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public boolean isPrimary() {
        return isPrimary;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
