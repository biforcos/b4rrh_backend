package com.b4rrhh.employee.contact.infrastructure.web.dto;

public record CreateContactRequest(
        String contactTypeCode,
        String contactValue
) {
}
