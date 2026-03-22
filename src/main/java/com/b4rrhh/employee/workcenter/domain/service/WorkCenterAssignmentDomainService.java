package com.b4rrhh.employee.workcenter.domain.service;

import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import com.b4rrhh.employee.workcenter.domain.port.EmployeePresencePort;
import com.b4rrhh.employee.workcenter.domain.port.RuleEntityValidationPort;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterAssignmentRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkCenterAssignmentDomainService {

    private final WorkCenterAssignmentRepository workCenterAssignmentRepository;
    private final EmployeePresencePort employeePresencePort;
    private final RuleEntityValidationPort ruleEntityValidationPort;

    public WorkCenterAssignmentDomainService(
            WorkCenterAssignmentRepository workCenterAssignmentRepository,
            EmployeePresencePort employeePresencePort,
            RuleEntityValidationPort ruleEntityValidationPort
    ) {
        this.workCenterAssignmentRepository = workCenterAssignmentRepository;
        this.employeePresencePort = employeePresencePort;
        this.ruleEntityValidationPort = ruleEntityValidationPort;
    }

    public void validateCreate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            WorkCenterAssignment assignment
    ) {
        validateCatalog(ruleSystemCode, assignment.getWorkCenterCode(), assignment.getStartDate());
        validateContainedInPresence(ruleSystemCode, employeeTypeCode, employeeNumber, assignment);

        if (workCenterAssignmentRepository.existsOverlappingPeriod(
                assignment.getEmployeeId(),
                assignment.getStartDate(),
                assignment.getEndDate()
        )) {
            throw new WorkCenterOverlapException(ruleSystemCode, employeeTypeCode, employeeNumber);
        }
    }

    public void validateUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            WorkCenterAssignment assignment
    ) {
        validateCatalog(ruleSystemCode, assignment.getWorkCenterCode(), assignment.getStartDate());
        validateContainedInPresence(ruleSystemCode, employeeTypeCode, employeeNumber, assignment);

        if (workCenterAssignmentRepository.existsOverlappingPeriodExcludingAssignment(
                assignment.getEmployeeId(),
                assignment.getWorkCenterAssignmentNumber(),
                assignment.getStartDate(),
                assignment.getEndDate()
        )) {
            throw new WorkCenterOverlapException(ruleSystemCode, employeeTypeCode, employeeNumber);
        }
    }

    private void validateCatalog(String ruleSystemCode, String workCenterCode, java.time.LocalDate startDate) {
        if (!ruleEntityValidationPort.existsActiveWorkCenterCode(ruleSystemCode, workCenterCode, startDate)) {
            throw new WorkCenterCatalogValueInvalidException("workCenterCode", workCenterCode);
        }
    }

    private void validateContainedInPresence(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            WorkCenterAssignment assignment
    ) {
        if (!employeePresencePort.existsPresenceContainingPeriod(
                assignment.getEmployeeId(),
                assignment.getStartDate(),
                assignment.getEndDate()
        )) {
            throw new WorkCenterOutsidePresencePeriodException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    assignment.getStartDate(),
                    assignment.getEndDate()
            );
        }
    }
}
