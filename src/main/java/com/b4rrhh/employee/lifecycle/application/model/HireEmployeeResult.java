package com.b4rrhh.employee.lifecycle.application.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record HireEmployeeResult(
        EmployeeSummary employee,
        PresenceSummary presence,
        WorkCenterSummary workCenter,
        CostCenterSummary costCenter,
        ContractSummary contract,
        LaborClassificationSummary laborClassification,
        WorkingTimeSummary workingTime
) {
    public record EmployeeSummary(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String firstName,
            String lastName1,
            String lastName2,
            String preferredName,
            String displayName,
            String status,
            LocalDate hireDate
    ) {}

    public record PresenceSummary(
            Integer presenceNumber,
            LocalDate startDate,
            String companyCode,
            String entryReasonCode
    ) {}

    public record WorkCenterSummary(
            LocalDate startDate,
            String workCenterCode,
            String workCenterName
    ) {}

    public record CostCenterSummary(
            LocalDate startDate,
            Double totalAllocationPercentage,
            List<CostCenterItemSummary> items
    ) {}

    public record CostCenterItemSummary(
            String costCenterCode,
            String costCenterName,
            Double allocationPercentage
    ) {}

    public record ContractSummary(
            LocalDate startDate,
            String contractTypeCode,
            String contractSubtypeCode
    ) {}

    public record LaborClassificationSummary(
            LocalDate startDate,
            String agreementCode,
            String agreementCategoryCode
    ) {}

    public record WorkingTimeSummary(
            Integer workingTimeNumber,
            BigDecimal workingTimePercentage,
            BigDecimal weeklyHours,
            BigDecimal dailyHours,
            BigDecimal monthlyHours,
            LocalDate startDate,
            LocalDate endDate
    ) {}
}
