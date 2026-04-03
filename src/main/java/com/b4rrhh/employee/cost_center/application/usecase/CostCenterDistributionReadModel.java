package com.b4rrhh.employee.cost_center.application.usecase;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Enriched read model for a distribution window, used as output from use cases.
 * Includes costCenterName looked up from the catalog.
 */
public class CostCenterDistributionReadModel {

    public record Item(
            String costCenterCode,
            String costCenterName,
            BigDecimal allocationPercentage
    ) {
    }

    public record Window(
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal totalAllocationPercentage,
            List<Item> items
    ) {
    }

    public record CurrentDistribution(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Window currentDistribution
    ) {
    }

    public record History(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            List<Window> windows
    ) {
    }
}
