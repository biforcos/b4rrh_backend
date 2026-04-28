package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputAlreadyExistsException;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEmployeePayrollInputService implements CreateEmployeePayrollInputUseCase {

    private final EmployeePayrollInputRepository repository;

    public CreateEmployeePayrollInputService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public EmployeePayrollInput create(CreateEmployeePayrollInputCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        String cc  = command.conceptCode().trim().toUpperCase();

        if (repository.existsByBusinessKey(rsc, etc, en, cc, command.period())) {
            throw new EmployeePayrollInputAlreadyExistsException(cc, command.period());
        }

        EmployeePayrollInput input = EmployeePayrollInput.create(rsc, etc, en, cc,
                command.period(), command.quantity());
        return repository.save(input);
    }
}
