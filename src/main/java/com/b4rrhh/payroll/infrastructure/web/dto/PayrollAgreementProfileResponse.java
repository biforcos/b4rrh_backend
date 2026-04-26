package com.b4rrhh.payroll.infrastructure.web.dto;

public record PayrollAgreementProfileResponse(
        String officialAgreementNumber,
        String displayName,
        String shortName,
        String annualHours,
        String agreementCategoryCode
) {}
