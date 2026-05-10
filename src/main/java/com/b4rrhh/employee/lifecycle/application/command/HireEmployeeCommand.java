package com.b4rrhh.employee.lifecycle.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HireEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        LocalDate hireDate,
        String entryReasonCode,
        String companyCode,
        String workCenterCode,
        HireEmployeeContractCommand contract,
        HireEmployeeLaborClassificationCommand laborClassification,
        HireEmployeeCostCenterDistributionCommand costCenterDistribution,
        HireEmployeeWorkingTimeCommand workingTime
) {
    public record HireEmployeeContractCommand(
            String contractTypeCode,
            String contractSubtypeCode
    ) {}

    public record HireEmployeeLaborClassificationCommand(
            String agreementCode,
            String agreementCategoryCode
    ) {}

    public record HireEmployeeCostCenterDistributionCommand(
            List<HireEmployeeCostCenterItemCommand> items
    ) {}

    public record HireEmployeeCostCenterItemCommand(
            String costCenterCode,
            Double allocationPercentage
    ) {}

    public record HireEmployeeWorkingTimeCommand(
            BigDecimal workingTimePercentage
    ) {}
}
