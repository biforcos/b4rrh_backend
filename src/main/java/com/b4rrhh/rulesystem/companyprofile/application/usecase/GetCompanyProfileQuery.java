package com.b4rrhh.rulesystem.companyprofile.application.usecase;

public record GetCompanyProfileQuery(
        String ruleSystemCode,
        String companyCode
) {
}