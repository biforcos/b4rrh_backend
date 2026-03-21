package com.b4rrhh.employee.contact.infrastructure.persistence;

import com.b4rrhh.employee.contact.application.port.EmployeeContactContext;
import com.b4rrhh.employee.contact.application.port.EmployeeContactLookupPort;
import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeOwnedLookupSupport;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeContactLookupAdapter implements EmployeeContactLookupPort {

    private final EmployeeOwnedLookupSupport employeeOwnedLookupSupport;

    public EmployeeContactLookupAdapter(EmployeeOwnedLookupSupport employeeOwnedLookupSupport) {
        this.employeeOwnedLookupSupport = employeeOwnedLookupSupport;
    }

    @Override
    public Optional<EmployeeContactContext> findByBusinessKey(
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
    public Optional<EmployeeContactContext> findByBusinessKeyForUpdate(
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

    private EmployeeContactContext toContext(EmployeeEntity employee) {
        return new EmployeeContactContext(
                employee.getId(),
                employee.getRuleSystemCode(),
                employee.getEmployeeTypeCode(),
                employee.getEmployeeNumber()
        );
    }
}
