package com.b4rrhh.employee.lifecycle.application.command;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RehireEmployeeCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate rehireDate,
        String entryReasonCode,
        String companyCode,
        String agreementCode,
        String agreementCategoryCode,
        String contractTypeCode,
        String contractSubtypeCode,
        String workCenterCode,
        RehireEmployeeCostCenterDistributionCommand costCenterDistribution
) {
    public record RehireEmployeeCostCenterDistributionCommand(
            List<RehireEmployeeCostCenterItemCommand> items
    ) {}

    public record RehireEmployeeCostCenterItemCommand(
            String costCenterCode,
            BigDecimal allocationPercentage
    ) {}
}
