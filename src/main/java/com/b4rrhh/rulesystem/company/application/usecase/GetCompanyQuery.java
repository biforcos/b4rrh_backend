package com.b4rrhh.rulesystem.company.application.usecase;

public record GetCompanyQuery(
        String ruleSystemCode,
        String companyCode
) {
}
