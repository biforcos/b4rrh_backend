package com.b4rrhh.employee.payroll_input.application.usecase;

import com.b4rrhh.employee.payroll_input.domain.exception.EmployeePayrollInputNotFoundException;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteEmployeePayrollInputService implements DeleteEmployeePayrollInputUseCase {

    private final EmployeePayrollInputRepository repository;

    public DeleteEmployeePayrollInputService(EmployeePayrollInputRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public void delete(DeleteEmployeePayrollInputCommand command) {
        String rsc = command.ruleSystemCode().trim().toUpperCase();
        String etc = command.employeeTypeCode().trim().toUpperCase();
        String en  = command.employeeNumber().trim();
        String cc  = command.conceptCode().trim().toUpperCase();

        if (!repository.existsByBusinessKey(rsc, etc, en, cc, command.period())) {
            throw new EmployeePayrollInputNotFoundException(cc, command.period());
        }
        repository.deleteByBusinessKey(rsc, etc, en, cc, command.period());
    }
}
