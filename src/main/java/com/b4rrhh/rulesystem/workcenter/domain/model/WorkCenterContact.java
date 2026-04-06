package com.b4rrhh.rulesystem.workcenter.domain.model;

public class WorkCenterContact {

    private static final int CONTACT_TYPE_CODE_MAX_LENGTH = 30;
    private static final int CONTACT_VALUE_MAX_LENGTH = 300;

    private final Integer contactNumber;
    private final String contactTypeCode;
    private final String contactTypeName;
    private final String contactValue;

    public WorkCenterContact(
            Integer contactNumber,
            String contactTypeCode,
            String contactTypeName,
            String contactValue
    ) {
        this.contactNumber = normalizeRequiredContactNumber(contactNumber);
        this.contactTypeCode = normalizeRequiredCode("contactTypeCode", contactTypeCode, CONTACT_TYPE_CODE_MAX_LENGTH);
        this.contactTypeName = normalizeOptionalText("contactTypeName", contactTypeName, 200);
        this.contactValue = normalizeRequiredText("contactValue", contactValue, CONTACT_VALUE_MAX_LENGTH);
    }

    public WorkCenterContact update(String newContactTypeCode, String newContactValue) {
        return new WorkCenterContact(contactNumber, newContactTypeCode, null, newContactValue);
    }

    public WorkCenterContact withContactTypeName(String newContactTypeName) {
        return new WorkCenterContact(contactNumber, contactTypeCode, newContactTypeName, contactValue);
    }

    public Integer getContactNumber() {
        return contactNumber;
    }

    public String getContactTypeCode() {
        return contactTypeCode;
    }

    public String getContactTypeName() {
        return contactTypeName;
    }

    public String getContactValue() {
        return contactValue;
    }

    private Integer normalizeRequiredContactNumber(Integer value) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException("contactNumber must be greater than 0");
        }

        return value;
    }

    private String normalizeRequiredCode(String fieldName, String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim().toUpperCase();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }

        return normalized;
    }

    private String normalizeRequiredText(String fieldName, String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }

        return normalized;
    }

    private String normalizeOptionalText(String fieldName, String value, int maxLength) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }

        return normalized;
    }
}