package com.b4rrhh.rulesystem.companyprofile.domain.model;

public class CompanyProfile {

    private static final int LEGAL_NAME_MAX_LENGTH = 200;
    private static final int TAX_IDENTIFIER_MAX_LENGTH = 50;
    private static final int STREET_MAX_LENGTH = 300;
    private static final int CITY_MAX_LENGTH = 120;
    private static final int POSTAL_CODE_MAX_LENGTH = 20;
    private static final int REGION_CODE_MAX_LENGTH = 30;

    private final String legalName;
    private final String taxIdentifier;
    private final String street;
    private final String city;
    private final String postalCode;
    private final String regionCode;
    private final String countryCode;

    public CompanyProfile(
            String legalName,
            String taxIdentifier,
            String street,
            String city,
            String postalCode,
            String regionCode,
            String countryCode
    ) {
        this.legalName = normalizeRequiredText("legalName", legalName, LEGAL_NAME_MAX_LENGTH);
        this.taxIdentifier = normalizeOptionalText("taxIdentifier", taxIdentifier, TAX_IDENTIFIER_MAX_LENGTH);
        this.street = normalizeOptionalText("street", street, STREET_MAX_LENGTH);
        this.city = normalizeOptionalText("city", city, CITY_MAX_LENGTH);
        this.postalCode = normalizeOptionalText("postalCode", postalCode, POSTAL_CODE_MAX_LENGTH);
        this.regionCode = normalizeOptionalCode("regionCode", regionCode, REGION_CODE_MAX_LENGTH);
        this.countryCode = normalizeOptionalCode(countryCode);
    }

    public CompanyProfile update(
            String legalName,
            String taxIdentifier,
            String street,
            String city,
            String postalCode,
            String regionCode,
            String countryCode
    ) {
        return new CompanyProfile(
                legalName,
                taxIdentifier,
                street,
                city,
                postalCode,
                regionCode,
                countryCode
        );
    }

    public String getLegalName() {
        return legalName;
    }

    public String getTaxIdentifier() {
        return taxIdentifier;
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

    private String normalizeRequiredText(String fieldName, String value, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }

        String normalized = value.trim();
        validateLength(fieldName, normalized, maxLength);
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

        validateLength(fieldName, normalized, maxLength);
        return normalized;
    }

    private String normalizeOptionalCode(String value) {
        String normalized = normalizeOptionalText("countryCode", value, 3);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private String normalizeOptionalCode(String fieldName, String value, int maxLength) {
        String normalized = normalizeOptionalText(fieldName, value, maxLength);
        return normalized == null ? null : normalized.toUpperCase();
    }

    private void validateLength(String fieldName, String value, int maxLength) {
        if (value.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " exceeds max length " + maxLength);
        }
    }
}