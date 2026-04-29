package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ListEmployeePayrollInputsService implements ListEmployeePayrollInputsUseCase {

    private final EmployeePayrollInputRepository repository;

    public ListEmployeePayrollInputsService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeePayrollInput> listByEmployeeAndPeriod(ListEmployeePayrollInputsCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        return repository.findByEmployeeAndPeriod(rsc, etc, en, command.period());
    }
}
