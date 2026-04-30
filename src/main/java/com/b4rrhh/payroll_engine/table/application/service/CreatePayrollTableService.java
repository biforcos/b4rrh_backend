package com.b4rrhh.payroll_engine.table.application.service;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.object.domain.port.PayrollObjectRepository;
import com.b4rrhh.payroll_engine.table.application.usecase.CreatePayrollTableCommand;
import com.b4rrhh.payroll_engine.table.application.usecase.CreatePayrollTableUseCase;
import com.b4rrhh.payroll_engine.table.domain.exception.PayrollTableAlreadyExistsException;
import org.springframework.stereotype.Service;

@Service
public class CreatePayrollTableService implements CreatePayrollTableUseCase {

    private final PayrollObjectRepository objectRepository;

    public CreatePayrollTableService(PayrollObjectRepository objectRepository) {
        this.objectRepository = objectRepository;
    }

    @Override
    public PayrollObject create(CreatePayrollTableCommand command) {
        if (objectRepository.existsByBusinessKey(command.ruleSystemCode(), PayrollObjectTypeCode.TABLE, command.objectCode())) {
            throw new PayrollTableAlreadyExistsException(command.ruleSystemCode(), command.objectCode());
        }
        return objectRepository.save(new PayrollObject(
                null, command.ruleSystemCode(), PayrollObjectTypeCode.TABLE, command.objectCode(), null, null
        ));
    }
}
