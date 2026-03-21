package com.b4rrhh.employee.identifier.infrastructure.persistence;

import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierContext;
import com.b4rrhh.employee.identifier.application.port.EmployeeIdentifierLookupPort;
import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeOwnedLookupSupport;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeIdentifierLookupAdapter implements EmployeeIdentifierLookupPort {

    private final EmployeeOwnedLookupSupport employeeOwnedLookupSupport;

    public EmployeeIdentifierLookupAdapter(EmployeeOwnedLookupSupport employeeOwnedLookupSupport) {
        this.employeeOwnedLookupSupport = employeeOwnedLookupSupport;
    }

    @Override
    public Optional<EmployeeIdentifierContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        return employeeOwnedLookupSupport.findOwnedByBusinessKey(
            ruleSystemCode,
            employeeTypeCode,
            employeeNumber,
            employee -> Optional.of(toContext(employee))
        );
    }

    @Override
    public Optional<EmployeeIdentifierContext> findByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        return employeeOwnedLookupSupport.findOwnedByBusinessKeyForUpdate(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                employee -> Optional.of(toContext(employee))
        );
    }

    private EmployeeIdentifierContext toContext(EmployeeEntity employee) {
        return new EmployeeIdentifierContext(
                employee.getId(),
                employee.getRuleSystemCode(),
                employee.getEmployeeTypeCode(),
                employee.getEmployeeNumber()
        );
    }
}
