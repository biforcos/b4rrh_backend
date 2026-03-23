package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeRequestInvalidException;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HireEmployeeRequest;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HireEmployeeResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HiredContractResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HiredLaborClassificationResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HiredPresenceResponse;
import com.b4rrhh.employee.lifecycle.infrastructure.rest.dto.HiredWorkCenterResponse;
import org.springframework.stereotype.Component;

@Component
public class HireEmployeeWebMapper {

    public HireEmployeeCommand toCommand(HireEmployeeRequest request) {
        if (request == null) {
            throw new HireEmployeeRequestInvalidException("request body is required");
        }

        return new HireEmployeeCommand(
                request.ruleSystemCode(),
                request.employeeTypeCode(),
                request.employeeNumber(),
                request.firstName(),
                request.lastName1(),
                request.lastName2(),
                request.preferredName(),
                request.hireDate(),
                request.presence() != null ? request.presence().companyCode() : null,
                request.presence() != null ? request.presence().entryReasonCode() : null,
                request.laborClassification() != null ? request.laborClassification().agreementCode() : null,
                request.laborClassification() != null ? request.laborClassification().agreementCategoryCode() : null,
                request.contract() != null ? request.contract().contractTypeCode() : null,
                request.contract() != null ? request.contract().contractSubtypeCode() : null,
                request.workCenter() != null ? request.workCenter().workCenterCode() : null
        );
    }

    public HireEmployeeResponse toResponse(HireEmployeeResult result) {
        return new HireEmployeeResponse(
                result.ruleSystemCode(),
                result.employeeTypeCode(),
                result.employeeNumber(),
                result.firstName(),
                result.lastName1(),
                result.lastName2(),
                result.preferredName(),
                result.status(),
                result.hireDate(),
                new HiredPresenceResponse(
                        result.presenceNumber(),
                        result.companyCode(),
                        result.entryReasonCode(),
                        result.hireDate()
                ),
                new HiredLaborClassificationResponse(
                        result.agreementCode(),
                        result.agreementCategoryCode(),
                        result.hireDate()
                ),
                new HiredContractResponse(
                        result.contractTypeCode(),
                        result.contractSubtypeCode(),
                        result.hireDate()
                ),
                new HiredWorkCenterResponse(
                        result.workCenterAssignmentNumber(),
                        result.workCenterCode(),
                        result.hireDate()
                )
        );
    }
}
