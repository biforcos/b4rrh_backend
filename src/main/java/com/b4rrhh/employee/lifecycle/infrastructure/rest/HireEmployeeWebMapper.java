package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterDistributionItemResponse;
import com.b4rrhh.employee.cost_center.infrastructure.web.dto.CostCenterDistributionWindowResponse;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeDefaultValues;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeRequestInvalidException;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HireEmployeeRequest;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HireEmployeeResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Component
public class HireEmployeeWebMapper {

    public HireEmployeeCommand toCommand(HireEmployeeRequest request) {
        if (request == null) {
            throw new HireEmployeeRequestInvalidException("request body is required");
        }

        return new HireEmployeeCommand(
                normalizeCode(request.ruleSystemCode()),
                normalizeEmployeeTypeCode(request.employeeTypeCode()),
                request.employeeNumber(),
                request.firstName(),
                request.lastName1(),
                request.lastName2(),
                request.preferredName(),
                request.hireDate(),
                request.entryReasonCode(),
                request.companyCode(),
                request.workCenterCode(),
                request.contract() != null ? new HireEmployeeCommand.HireEmployeeContractCommand(
                        request.contract().contractTypeCode(),
                        request.contract().contractSubtypeCode()
                ) : null,
                request.laborClassification() != null ? new HireEmployeeCommand.HireEmployeeLaborClassificationCommand(
                        request.laborClassification().agreementCode(),
                        request.laborClassification().agreementCategoryCode()
                ) : null,
                request.costCenterDistribution() != null ? new HireEmployeeCommand.HireEmployeeCostCenterDistributionCommand(
                        request.costCenterDistribution().items().stream()
                                .map(item -> new HireEmployeeCommand.HireEmployeeCostCenterItemCommand(
                                         item.costCenterCode(),
                                         item.allocationPercentage()
                                ))
                                .collect(Collectors.toList())
                ) : null
        );
    }

    private String normalizeCode(String value) {
        return value != null ? value.trim().toUpperCase() : null;
    }

    private String normalizeEmployeeTypeCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return HireEmployeeDefaultValues.DEFAULT_EMPLOYEE_TYPE_CODE;
        }
        return value.trim().toUpperCase();
    }

    public HireEmployeeResponse toResponse(HireEmployeeResult result) {
        return new HireEmployeeResponse(
                result.employee().ruleSystemCode(),
                result.employee().employeeTypeCode(),
                result.employee().employeeNumber(),
                result.employee().firstName(),
                result.employee().lastName1(),
                result.employee().lastName2(),
                result.employee().preferredName(),
                result.employee().status(),
                result.employee().hireDate(),
                new HireEmployeeResponse.PresenceSummary(
                        result.presence().presenceNumber(),
                        result.presence().startDate(),
                        result.presence().companyCode(),
                        result.presence().entryReasonCode()
                ),
                new HireEmployeeResponse.WorkCenterSummary(
                         result.workCenter().startDate(),
                         result.workCenter().workCenterCode(),
                         result.workCenter().workCenterName()
                ),
                result.costCenter() != null ? new CostCenterDistributionWindowResponse(
                        result.costCenter().startDate(),
                        null,
                        BigDecimal.valueOf(result.costCenter().totalAllocationPercentage()),
                        result.costCenter().items().stream()
                                .map(item -> new CostCenterDistributionItemResponse(
                                        item.costCenterCode(),
                                        item.costCenterName(),
                                        BigDecimal.valueOf(item.allocationPercentage())
                                ))
                                .collect(Collectors.toList())
                ) : null,
                new HireEmployeeResponse.ContractSummary(
                        result.contract().startDate(),
                        result.contract().contractTypeCode(),
                        result.contract().contractSubtypeCode()
                ),
                new HireEmployeeResponse.LaborClassificationSummary(
                        result.laborClassification().startDate(),
                        result.laborClassification().agreementCode(),
                        result.laborClassification().agreementCategoryCode()
                )
        );
    }
}
