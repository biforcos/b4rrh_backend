package com.b4rrhh.rulesystem.companyprofile.application.service;

import org.springframework.stereotype.Component;

@Component
public class CompanyProfileInputNormalizer {

    public String normalizeRequiredRuleSystemCode(String ruleSystemCode) {
        return normalizeRequiredCode("ruleSystemCode", ruleSystemCode, 5);
    }

    public String normalizeRequiredCompanyCode(String companyCode) {
        return normalizeRequiredCode("companyCode", companyCode, 30);
    }

    public String normalizeOptionalCountryCode(String countryCode) {
        if (countryCode == null) {
            return null;
        }

        String normalized = countryCode.trim();
        if (normalized.isEmpty()) {
            return null;
        }

        normalized = normalized.toUpperCase();
        if (normalized.length() != 3) {
            throw new IllegalArgumentException("countryCode must have length 3");
        }

        return normalized;
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
}