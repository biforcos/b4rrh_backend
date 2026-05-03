package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

public record UpsertAgreementCategoryProfileCommand(
        String ruleSystemCode,
        String categoryCode,
        String grupoCotizacionCode,
        String tipoNomina
) {}
