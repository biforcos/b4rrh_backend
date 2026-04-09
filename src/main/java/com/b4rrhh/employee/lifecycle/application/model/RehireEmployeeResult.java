package com.b4rrhh.employee.lifecycle.application.model;

import java.time.LocalDate;
import java.util.List;

public record RehireEmployeeResult(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate rehireDate,
        String status,
        Integer newPresenceNumber,
        String newPresenceCompanyCode,
        String newPresenceEntryReasonCode,
        LocalDate newPresenceStartDate,
        String newContractTypeCode,
        String newContractSubtypeCode,
        LocalDate newContractStartDate,
        String newAgreementCode,
        String newAgreementCategoryCode,
        LocalDate newLaborClassificationStartDate,
        Integer newWorkCenterAssignmentNumber,
        String newWorkCenterCode,
        LocalDate newWorkCenterStartDate,
        CostCenterSummary newCostCenter,
        WorkingTimeSummary newWorkingTime,
        boolean created
) {
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

    public record WorkingTimeSummary(
            Integer workingTimeNumber,
            java.math.BigDecimal workingTimePercentage,
            java.math.BigDecimal weeklyHours,
            java.math.BigDecimal dailyHours,
            java.math.BigDecimal monthlyHours,
            LocalDate startDate,
            LocalDate endDate
    ) {}
}
