package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterCatalogValidator;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterPresenceConsistencyValidator;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterEmployeeNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterRuleSystemNotFoundException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service("employeeCreateWorkCenterService")
public class CreateWorkCenterService implements CreateWorkCenterUseCase {

    private final WorkCenterRepository workCenterRepository;
    private final EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final WorkCenterCatalogValidator workCenterCatalogValidator;
    private final WorkCenterPresenceConsistencyValidator workCenterPresenceConsistencyValidator;

    public CreateWorkCenterService(
            WorkCenterRepository workCenterRepository,
            EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort,
            RuleSystemRepository ruleSystemRepository,
            WorkCenterCatalogValidator workCenterCatalogValidator,
            WorkCenterPresenceConsistencyValidator workCenterPresenceConsistencyValidator
    ) {
        this.workCenterRepository = workCenterRepository;
        this.employeeWorkCenterLookupPort = employeeWorkCenterLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
        this.workCenterCatalogValidator = workCenterCatalogValidator;
        this.workCenterPresenceConsistencyValidator = workCenterPresenceConsistencyValidator;
    }

    @Override
    @Transactional
    public WorkCenter create(CreateWorkCenterCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

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

        String workCenterCode = workCenterCatalogValidator.normalizeRequiredCode("workCenterCode", command.workCenterCode());
        workCenterCatalogValidator.validateWorkCenterCode(normalizedRuleSystemCode, workCenterCode, command.startDate());

        int nextAssignmentNumber = workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(employee.employeeId())
                .map(value -> value + 1)
                .orElse(1);

        WorkCenter newWorkCenter = new WorkCenter(
                null,
                employee.employeeId(),
                nextAssignmentNumber,
                workCenterCode,
                command.startDate(),
                command.endDate(),
                null,
                null
        );

        if (workCenterRepository.existsOverlappingPeriod(
                employee.employeeId(),
                newWorkCenter.getStartDate(),
                newWorkCenter.getEndDate()
        )) {
            throw new WorkCenterOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber
            );
        }

        workCenterPresenceConsistencyValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                newWorkCenter.getStartDate(),
                newWorkCenter.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<WorkCenter> projectedHistory = new ArrayList<>(
                workCenterRepository.findByEmployeeIdOrderByStartDate(employee.employeeId())
        );
        projectedHistory.add(newWorkCenter);

        workCenterPresenceConsistencyValidator.validatePresenceCoverageIfRequired(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        return workCenterRepository.save(newWorkCenter);
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
}