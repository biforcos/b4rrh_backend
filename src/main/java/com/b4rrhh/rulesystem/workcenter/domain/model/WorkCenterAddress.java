package com.b4rrhh.rulesystem.workcenter.domain.model;

public class WorkCenterAddress {

    private static final int STREET_MAX_LENGTH = 300;
    private static final int CITY_MAX_LENGTH = 120;
    private static final int POSTAL_CODE_MAX_LENGTH = 20;
    private static final int REGION_CODE_MAX_LENGTH = 30;

    private final String street;
    private final String city;
    private final String postalCode;
    private final String regionCode;
    private final String countryCode;

    public WorkCenterAddress(
            String street,
            String city,
            String postalCode,
            String regionCode,
            String countryCode
    ) {
        this.street = normalizeOptionalText("street", street, STREET_MAX_LENGTH);
        this.city = normalizeOptionalText("city", city, CITY_MAX_LENGTH);
        this.postalCode = normalizeOptionalText("postalCode", postalCode, POSTAL_CODE_MAX_LENGTH);
        this.regionCode = normalizeOptionalCode("regionCode", regionCode, REGION_CODE_MAX_LENGTH);
        this.countryCode = normalizeOptionalCountryCode(countryCode);
    }

    public static WorkCenterAddress empty() {
        return new WorkCenterAddress(null, null, null, null, null);
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getRegionCode() {
        return regionCode;
    }

    public String getCountryCode() {
        return countryCode;
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

    private String normalizeOptionalCode(String fieldName, String value, int maxLength) {
        String normalized = normalizeOptionalText(fieldName, value, maxLength);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String normalizeOptionalCountryCode(String value) {
        String normalized = normalizeOptionalText("countryCode", value, 3);
        return normalized == null ? null : normalized.toUpperCase();
    }
}