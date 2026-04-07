package com.b4rrhh.employee.working_time.infrastructure.persistence;

import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeBusinessKeyLookupSupport;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeWorkingTimeLookupAdapter implements EmployeeWorkingTimeLookupPort {

    private final EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport;

    public EmployeeWorkingTimeLookupAdapter(EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport) {
        this.employeeBusinessKeyLookupSupport = employeeBusinessKeyLookupSupport;
    }

    @Override
    public Optional<EmployeeWorkingTimeContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        return employeeBusinessKeyLookupSupport
                .findByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .map(this::toContext);
    }

    @Override
    public Optional<EmployeeWorkingTimeContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        return employeeBusinessKeyLookupSupport
                .findByBusinessKeyForUpdate(ruleSystemCode, employeeTypeCode, employeeNumber)
                .map(this::toContext);
    }

    private EmployeeWorkingTimeContext toContext(EmployeeEntity employeeEntity) {
        return new EmployeeWorkingTimeContext(
                employeeEntity.getId(),
                employeeEntity.getRuleSystemCode(),
                employeeEntity.getEmployeeTypeCode(),
                employeeEntity.getEmployeeNumber()
        );
    }
}