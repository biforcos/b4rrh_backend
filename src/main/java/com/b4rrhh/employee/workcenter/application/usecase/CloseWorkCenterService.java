package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterPresenceConsistencyValidator;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterEmployeeNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterRuleSystemNotFoundException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CloseWorkCenterService implements CloseWorkCenterUseCase {

    private final WorkCenterRepository workCenterRepository;
    private final EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final WorkCenterPresenceConsistencyValidator workCenterPresenceConsistencyValidator;

    public CloseWorkCenterService(
            WorkCenterRepository workCenterRepository,
            EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort,
            RuleSystemRepository ruleSystemRepository,
            WorkCenterPresenceConsistencyValidator workCenterPresenceConsistencyValidator
    ) {
        this.workCenterRepository = workCenterRepository;
        this.employeeWorkCenterLookupPort = employeeWorkCenterLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
        this.workCenterPresenceConsistencyValidator = workCenterPresenceConsistencyValidator;
    }

    @Override
    @Transactional
    public WorkCenter close(CloseWorkCenterCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        Integer normalizedAssignmentNumber = normalizeAssignmentNumber(command.workCenterAssignmentNumber());

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new WorkCenterRuleSystemNotFoundException(normalizedRuleSystemCode));

        EmployeeWorkCenterContext employee = employeeWorkCenterLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new WorkCenterEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        WorkCenter existing = workCenterRepository
                .findByEmployeeIdAndWorkCenterAssignmentNumber(employee.employeeId(), normalizedAssignmentNumber)
                .orElseThrow(() -> new WorkCenterNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedAssignmentNumber
                ));

        WorkCenter closed = existing.close(command.endDate());

        workCenterPresenceConsistencyValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                closed.getStartDate(),
                closed.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<WorkCenter> projectedHistory = replaceWithClosedVersion(
                workCenterRepository.findByEmployeeIdOrderByStartDate(employee.employeeId()),
                closed
        );

        workCenterPresenceConsistencyValidator.validatePresenceCoverageIfRequired(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        return workCenterRepository.save(closed);
    }

    private List<WorkCenter> replaceWithClosedVersion(List<WorkCenter> history, WorkCenter closed) {
        List<WorkCenter> projected = new ArrayList<>(history.size());
        for (WorkCenter item : history) {
            if (item.getWorkCenterAssignmentNumber().equals(closed.getWorkCenterAssignmentNumber())) {
                projected.add(closed);
            } else {
                projected.add(item);
            }
        }

        return projected;
    }

    private String normalizeRuleSystemCode(String ruleSystemCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }

        return ruleSystemCode.trim().toUpperCase();
    }

    private String normalizeEmployeeTypeCode(String employeeTypeCode) {
        if (employeeTypeCode == null || employeeTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeTypeCode is required");
        }

        return employeeTypeCode.trim().toUpperCase();
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }

        return employeeNumber.trim();
    }

    private Integer normalizeAssignmentNumber(Integer workCenterAssignmentNumber) {
        if (workCenterAssignmentNumber == null || workCenterAssignmentNumber <= 0) {
            throw new IllegalArgumentException("workCenterAssignmentNumber must be a positive integer");
        }

        return workCenterAssignmentNumber;
    }
}