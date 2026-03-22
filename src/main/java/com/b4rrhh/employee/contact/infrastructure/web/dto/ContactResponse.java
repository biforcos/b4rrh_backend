package com.b4rrhh.employee.contact.infrastructure.web.dto;

public record ContactResponse(
        String contactTypeCode,
        String contactTypeName,
        String contactValue
) {
}
