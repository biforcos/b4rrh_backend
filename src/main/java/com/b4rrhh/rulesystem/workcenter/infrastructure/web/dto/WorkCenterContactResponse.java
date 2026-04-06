package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

public record WorkCenterContactResponse(
        Integer contactNumber,
        String contactTypeCode,
        String contactTypeName,
        String contactValue
) {
}