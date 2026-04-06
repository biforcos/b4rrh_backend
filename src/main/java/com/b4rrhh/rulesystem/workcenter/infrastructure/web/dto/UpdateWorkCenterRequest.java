package com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto;

public record UpdateWorkCenterRequest(
        String name,
        String description,
        String companyCode,
        WorkCenterAddress address
) {
}