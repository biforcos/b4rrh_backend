package com.b4rrhh.employee.tax_information.infrastructure.web.dto;

public record EmployeeTaxInformationResponse(
    String validFrom,
    String familySituation,
    int descendantsCount,
    int ascendantsCount,
    String disabilityDegree,
    boolean pensionCompensatoria,
    boolean geographicMobility,
    boolean habitualResidenceLoan,
    String taxTerritory
) {}
