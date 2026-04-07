package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.WorkingTimePresenceConsistencyValidator;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeEmployeeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNumberConflictException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOverlapException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateWorkingTimeService implements CreateWorkingTimeUseCase {

    private final WorkingTimeRepository workingTimeRepository;
    private final EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;
    private final WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator;
    private final WorkingTimeDerivationPolicy workingTimeDerivationPolicy;

    public CreateWorkingTimeService(
            WorkingTimeRepository workingTimeRepository,
            EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort,
            WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator,
            WorkingTimeDerivationPolicy workingTimeDerivationPolicy
    ) {
        this.workingTimeRepository = workingTimeRepository;
        this.employeeWorkingTimeLookupPort = employeeWorkingTimeLookupPort;
        this.workingTimePresenceConsistencyValidator = workingTimePresenceConsistencyValidator;
        this.workingTimeDerivationPolicy = workingTimeDerivationPolicy;
    }

    @Override
    @Transactional
    public WorkingTime create(CreateWorkingTimeCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        EmployeeWorkingTimeContext employee = employeeWorkingTimeLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new WorkingTimeEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        int nextWorkingTimeNumber = workingTimeRepository.findMaxWorkingTimeNumberByEmployeeId(employee.employeeId())
                .map(value -> value + 1)
                .orElse(1);

        WorkingTimeDerivedHours derivedHours = workingTimeDerivationPolicy.derive(command.workingTimePercentage());

        WorkingTime newWorkingTime = WorkingTime.create(
                employee.employeeId(),
                nextWorkingTimeNumber,
                command.startDate(),
                null,
                command.workingTimePercentage(),
            derivedHours,
            workingTimeDerivationPolicy
        );

        if (workingTimeRepository.existsOverlappingPeriod(
                employee.employeeId(),
                newWorkingTime.getStartDate(),
                newWorkingTime.getEndDate()
        )) {
            throw new WorkingTimeOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    newWorkingTime.getStartDate(),
                    newWorkingTime.getEndDate()
            );
        }

        workingTimePresenceConsistencyValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                newWorkingTime.getStartDate(),
                newWorkingTime.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        try {
            return workingTimeRepository.save(newWorkingTime);
        } catch (DataIntegrityViolationException ex) {
            throw new WorkingTimeNumberConflictException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    nextWorkingTimeNumber,
                    ex
            );
        }
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