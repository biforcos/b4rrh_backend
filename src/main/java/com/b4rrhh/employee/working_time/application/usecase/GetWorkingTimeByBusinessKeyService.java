package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeEmployeeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNotFoundException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetWorkingTimeByBusinessKeyService implements GetWorkingTimeByBusinessKeyUseCase {

    private final WorkingTimeRepository workingTimeRepository;
    private final EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;

    public GetWorkingTimeByBusinessKeyService(
            WorkingTimeRepository workingTimeRepository,
            EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort
    ) {
        this.workingTimeRepository = workingTimeRepository;
        this.employeeWorkingTimeLookupPort = employeeWorkingTimeLookupPort;
    }

    @Override
    @Transactional(readOnly = true)
    public WorkingTime getByBusinessKey(GetWorkingTimeByBusinessKeyCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        Integer normalizedWorkingTimeNumber = normalizeWorkingTimeNumber(command.workingTimeNumber());

        EmployeeWorkingTimeContext employee = employeeWorkingTimeLookupPort
                .findByBusinessKey(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new WorkingTimeEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        return workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(
                        employee.employeeId(),
                        normalizedWorkingTimeNumber
                )
                .orElseThrow(() -> new WorkingTimeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedWorkingTimeNumber
                ));
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

    private Integer normalizeWorkingTimeNumber(Integer workingTimeNumber) {
        if (workingTimeNumber == null || workingTimeNumber <= 0) {
            throw new IllegalArgumentException("workingTimeNumber must be a positive integer");
        }

        return workingTimeNumber;
    }
}