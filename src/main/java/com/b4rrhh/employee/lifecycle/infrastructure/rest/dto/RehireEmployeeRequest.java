package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record RehireEmployeeRequest(
        LocalDate rehireDate,
        String entryReasonCode,
        String companyCode,
        RehireLaborClassificationRequest laborClassification,
        RehireContractRequest contract,
        RehireWorkCenterRequest workCenter,
        RehireCostCenterDistributionRequest costCenterDistribution
) {
    public record RehireCostCenterDistributionRequest(
            List<RehireCostCenterItemRequest> items
    ) {}

    public record RehireCostCenterItemRequest(
            String costCenterCode,
            BigDecimal allocationPercentage
    ) {}
}
