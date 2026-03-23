package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminateEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeRequestInvalidException;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.ClosedContractResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.ClosedLaborClassificationResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.ClosedPresenceResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.ClosedWorkCenterResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.TerminateEmployeeRequest;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.TerminateEmployeeResponse;
import org.springframework.stereotype.Component;

@Component
public class TerminateEmployeeWebMapper {

    public TerminateEmployeeCommand toCommand(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            TerminateEmployeeRequest request
    ) {
        if (request == null) {
            throw new TerminateEmployeeRequestInvalidException("request body is required");
        }

        return new TerminateEmployeeCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                request.terminationDate(),
                request.exitReasonCode()
        );
    }

    public TerminateEmployeeResponse toResponse(TerminateEmployeeResult result) {
        return new TerminateEmployeeResponse(
                result.ruleSystemCode(),
                result.employeeTypeCode(),
                result.employeeNumber(),
                result.terminationDate(),
                result.exitReasonCode(),
                result.status(),
                new ClosedPresenceResponse(
                        result.closedPresenceNumber(),
                        result.closedPresenceCompanyCode(),
                        result.closedPresenceEntryReasonCode(),
                        result.closedPresenceExitReasonCode(),
                        result.closedPresenceStartDate(),
                        result.closedPresenceEndDate()
                ),
                new ClosedContractResponse(
                        result.closedContractTypeCode(),
                        result.closedContractSubtypeCode(),
                        result.closedContractStartDate(),
                        result.closedContractEndDate()
                ),
                new ClosedLaborClassificationResponse(
                        result.closedAgreementCode(),
                        result.closedAgreementCategoryCode(),
                        result.closedLaborClassificationStartDate(),
                        result.closedLaborClassificationEndDate()
                ),
                new ClosedWorkCenterResponse(
                        result.closedWorkCenterAssignmentNumber(),
                        result.closedWorkCenterCode(),
                        result.closedWorkCenterStartDate(),
                        result.closedWorkCenterEndDate()
                )
        );
    }
}
