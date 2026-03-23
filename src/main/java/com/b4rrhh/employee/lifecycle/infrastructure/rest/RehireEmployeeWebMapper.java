package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.RehireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.RehireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.RehireEmployeeRequestInvalidException;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehireEmployeeRequest;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehireEmployeeResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehiredContractResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehiredLaborClassificationResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehiredPresenceResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.RehiredWorkCenterResponse;
import org.springframework.stereotype.Component;

@Component
public class RehireEmployeeWebMapper {

    public RehireEmployeeCommand toCommand(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            RehireEmployeeRequest request
    ) {
        if (request == null) {
            throw new RehireEmployeeRequestInvalidException("request body is required");
        }

        return new RehireEmployeeCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                request.rehireDate(),
                request.entryReasonCode(),
                request.companyCode(),
                request.laborClassification() != null ? request.laborClassification().agreementCode() : null,
                request.laborClassification() != null ? request.laborClassification().agreementCategoryCode() : null,
                request.contract() != null ? request.contract().contractTypeCode() : null,
                request.contract() != null ? request.contract().contractSubtypeCode() : null,
                request.workCenter() != null ? request.workCenter().workCenterCode() : null
        );
    }

    public RehireEmployeeResponse toResponse(RehireEmployeeResult result) {
        return new RehireEmployeeResponse(
                result.ruleSystemCode(),
                result.employeeTypeCode(),
                result.employeeNumber(),
                result.rehireDate(),
                result.status(),
                new RehiredPresenceResponse(
                        result.newPresenceNumber(),
                        result.newPresenceCompanyCode(),
                        result.newPresenceEntryReasonCode(),
                        result.newPresenceStartDate()
                ),
                new RehiredContractResponse(
                        result.newContractTypeCode(),
                        result.newContractSubtypeCode(),
                        result.newContractStartDate()
                ),
                new RehiredLaborClassificationResponse(
                        result.newAgreementCode(),
                        result.newAgreementCategoryCode(),
                        result.newLaborClassificationStartDate()
                ),
                new RehiredWorkCenterResponse(
                        result.newWorkCenterAssignmentNumber(),
                        result.newWorkCenterCode(),
                        result.newWorkCenterStartDate()
                )
        );
    }
}
