package com.b4rrhh.employee.identifier.application.service;

import com.b4rrhh.employee.identifier.domain.exception.IdentifierSpanishNationalIdInvalidException;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@Component
public class SpanishNationalIdValidator {

    private static final String NATIONAL_ID_TYPE_CODE = "NATIONAL_ID";
    private static final String SPAIN_COUNTRY_CODE = "ESP";
    private static final String LETTERS = "TRWAGMYFPDXBNJZSQVHLCKE";
    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{8}[A-Z]$");

    public String normalizeAndValidateIfApplicable(
            String identifierTypeCode,
            String issuingCountryCode,
            String identifierValue
    ) {
        if (identifierValue == null) {
            return null;
        }

        if (!applies(identifierTypeCode, issuingCountryCode)) {
            return identifierValue;
        }

        String normalizedValue = identifierValue.trim().toUpperCase();
        if (!DNI_PATTERN.matcher(normalizedValue).matches()) {
            throw new IdentifierSpanishNationalIdInvalidException();
        }

        int dniNumber = Integer.parseInt(normalizedValue.substring(0, 8));
        char expectedLetter = LETTERS.charAt(dniNumber % 23);
        char providedLetter = normalizedValue.charAt(8);
        if (providedLetter != expectedLetter) {
            throw new IdentifierSpanishNationalIdInvalidException();
        }

        return normalizedValue;
    }

    private boolean applies(String identifierTypeCode, String issuingCountryCode) {
        return NATIONAL_ID_TYPE_CODE.equals(identifierTypeCode)
                && SPAIN_COUNTRY_CODE.equals(issuingCountryCode);
    }
}