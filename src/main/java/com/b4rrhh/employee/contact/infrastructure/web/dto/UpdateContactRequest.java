package com.b4rrhh.employee.contact.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

public class UpdateContactRequest {

    private String contactValue;

    public String getContactValue() {
        return contactValue;
    }

    public void setContactValue(String contactValue) {
        this.contactValue = contactValue;
    }

    @JsonAnySetter
    public void rejectUnknown(String fieldName, Object ignoredValue) {
        throw new IllegalArgumentException(fieldName + " is not allowed in UpdateContactRequest");
    }
}
