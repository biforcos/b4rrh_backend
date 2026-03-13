package com.b4rrhh.employee.contact.domain.model;

import com.b4rrhh.employee.contact.domain.exception.ContactValueInvalidException;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class Contact {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[0-9+()\\-\\s]{6,20}$");
    private static final Pattern EXTENSION_PATTERN = Pattern.compile("^[A-Za-z0-9\\-]{1,10}$");

    private final Long id;
    private final Long employeeId;
    private final String contactTypeCode;
    private final String contactValue;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Contact(
            Long id,
            Long employeeId,
            String contactTypeCode,
            String contactValue,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.employeeId = employeeId;
        this.contactTypeCode = normalizeRequiredContactTypeCode(contactTypeCode);
        this.contactValue = normalizeAndValidateContactValue(this.contactTypeCode, contactValue);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Contact updateContactValue(String newContactValue) {
        return new Contact(
                id,
                employeeId,
                contactTypeCode,
                newContactValue,
                createdAt,
                updatedAt
        );
    }

    private String normalizeRequiredContactTypeCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ContactValueInvalidException("contactTypeCode is required");
        }

        return value.trim().toUpperCase();
    }

    private String normalizeAndValidateContactValue(String typeCode, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new ContactValueInvalidException("contactValue is required");
        }

        String normalizedValue = value.trim();

        switch (typeCode) {
            case "EMAIL" -> {
                if (!EMAIL_PATTERN.matcher(normalizedValue).matches()) {
                    throw new ContactValueInvalidException("contactValue is invalid for contactTypeCode EMAIL");
                }
            }
            case "PHONE", "MOBILE", "COMPANY_MOBILE" -> {
                if (!PHONE_PATTERN.matcher(normalizedValue).matches()) {
                    throw new ContactValueInvalidException(
                            "contactValue is invalid for contactTypeCode " + typeCode
                    );
                }
            }
            case "EXTENSION" -> {
                if (!EXTENSION_PATTERN.matcher(normalizedValue).matches()) {
                    throw new ContactValueInvalidException("contactValue is invalid for contactTypeCode EXTENSION");
                }
            }
            default -> {
                // Unknown contact types are catalog-governed; only non-empty value is enforced here.
            }
        }

        return normalizedValue;
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public String getContactTypeCode() { return contactTypeCode; }
    public String getContactValue() { return contactValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
