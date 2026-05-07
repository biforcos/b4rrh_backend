package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.domain.model.*;
import java.time.LocalDate;

public record CorrectEmployeeTaxInformationCommand(
    String ruleSystemCode, String employeeTypeCode, String employeeNumber,
    LocalDate validFrom,
    FamilySituation familySituation, int descendantsCount, int ascendantsCount,
    DisabilityDegree disabilityDegree,
    boolean pensionCompensatoria, boolean geographicMobility, boolean habitualResidenceLoan,
    TaxTerritory taxTerritory
) {}
