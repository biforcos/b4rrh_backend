package com.b4rrhh.employee.identifier.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.time.LocalDate;

public class UpdateIdentifierRequest {

    private String identifierValue;
    private String issuingCountryCode;
    private LocalDate expirationDate;
    private Boolean isPrimary;

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getIssuingCountryCode() {
        return issuingCountryCode;
    }

    public void setIssuingCountryCode(String issuingCountryCode) {
        this.issuingCountryCode = issuingCountryCode;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(Boolean primary) {
        isPrimary = primary;
    }

    @JsonAnySetter
    public void rejectUnknown(String fieldName, Object ignoredValue) {
        throw new IllegalArgumentException(fieldName + " is not allowed in UpdateIdentifierRequest");
    }
}
