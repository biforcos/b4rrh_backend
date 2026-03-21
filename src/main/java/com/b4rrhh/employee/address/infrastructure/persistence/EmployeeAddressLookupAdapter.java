package com.b4rrhh.employee.address.infrastructure.persistence;

import com.b4rrhh.employee.address.application.port.EmployeeAddressContext;
import com.b4rrhh.employee.address.application.port.EmployeeAddressLookupPort;
import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeOwnedLookupSupport;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeAddressLookupAdapter implements EmployeeAddressLookupPort {

    private final EmployeeOwnedLookupSupport employeeOwnedLookupSupport;

    public EmployeeAddressLookupAdapter(EmployeeOwnedLookupSupport employeeOwnedLookupSupport) {
        this.employeeOwnedLookupSupport = employeeOwnedLookupSupport;
    }

    @Override
    public Optional<EmployeeAddressContext> findByBusinessKey(
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
    public Optional<EmployeeAddressContext> findByBusinessKeyForUpdate(
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

    private EmployeeAddressContext toContext(EmployeeEntity employee) {
        return new EmployeeAddressContext(
                employee.getId(),
                employee.getRuleSystemCode(),
                employee.getEmployeeTypeCode(),
                employee.getEmployeeNumber()
        );
    }
}
