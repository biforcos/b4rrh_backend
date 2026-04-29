package com.b4rrhh.payroll_engine.eligibility.infrastructure.web;

import com.b4rrhh.payroll_engine.eligibility.application.usecase.CreateConceptAssignmentCommand;
import com.b4rrhh.payroll_engine.eligibility.application.usecase.UpdateConceptAssignmentCommand;
import com.b4rrhh.payroll_engine.eligibility.domain.model.ConceptAssignment;
import org.springframework.stereotype.Component;

@Component
public class ConceptAssignmentManagementAssembler {

    public ConceptAssignmentResponse toResponse(ConceptAssignment assignment) {
        return new ConceptAssignmentResponse(
                assignment.getId() == null ? null : String.valueOf(assignment.getId()),
                assignment.getRuleSystemCode(),
                assignment.getConceptCode(),
                assignment.getCompanyCode(),
                assignment.getAgreementCode(),
                assignment.getEmployeeTypeCode(),
                assignment.getValidFrom(),
                assignment.getValidTo(),
                assignment.getPriority()
        );
    }

    public CreateConceptAssignmentCommand toCommand(
            String ruleSystemCode,
            CreateConceptAssignmentRequest request
    ) {
        return new CreateConceptAssignmentCommand(
                ruleSystemCode,
                request.conceptCode(),
                request.companyCode(),
                request.agreementCode(),
                request.employeeTypeCode(),
                request.validFrom(),
                request.validTo(),
                request.priority()
        );
    }

    public UpdateConceptAssignmentCommand toUpdateCommand(
            String ruleSystemCode,
            String assignmentCode,
            UpdateConceptAssignmentRequest request
    ) {
        return new UpdateConceptAssignmentCommand(
                ruleSystemCode,
                assignmentCode,
                request.companyCode(),
                request.agreementCode(),
                request.employeeTypeCode(),
                request.validFrom(),
                request.validTo(),
                request.priority()
        );
    }
}
