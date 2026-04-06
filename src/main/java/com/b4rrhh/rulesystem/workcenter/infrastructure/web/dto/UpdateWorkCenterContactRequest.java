package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

public record UpdateWorkCenterContactRequest(
        String contactTypeCode,
        String contactValue
) {
}