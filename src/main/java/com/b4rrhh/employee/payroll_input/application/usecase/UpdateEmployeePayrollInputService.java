package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputNotFoundException;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateEmployeePayrollInputService implements UpdateEmployeePayrollInputUseCase {

    private final EmployeePayrollInputRepository repository;

    public UpdateEmployeePayrollInputService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EmployeePayrollInput update(UpdateEmployeePayrollInputCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        String cc  = command.conceptCode().trim().toUpperCase();

        EmployeePayrollInput input = repository
                .findByBusinessKey(rsc, etc, en, cc, command.period())
                .orElseThrow(() -> new EmployeePayrollInputNotFoundException(cc, command.period()));

        input.updateQuantity(command.quantity());
        return repository.save(input);
    }
}
