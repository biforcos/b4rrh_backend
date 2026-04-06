package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

public record CreateWorkCenterContactRequest(
        String contactTypeCode,
        String contactValue
) {
}