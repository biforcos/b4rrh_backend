package com.b4rrhh.payroll.basesalary.infrastructure.persistence;

import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeBusinessKeyLookupSupport;
import com.b4rrhh.payroll.basesalary.domain.EmployeeByBusinessKeyLookupPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeeByBusinessKeyLookupAdapter implements EmployeeByBusinessKeyLookupPort {

    private final EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport;

    public EmployeeByBusinessKeyLookupAdapter(EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport) {
        this.employeeBusinessKeyLookupSupport = employeeBusinessKeyLookupSupport;
    }

    @Override
    public Optional<Long> resolveEmployeeId(String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        return employeeBusinessKeyLookupSupport.findByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .map(employee -> employee.getId());
    }
}
