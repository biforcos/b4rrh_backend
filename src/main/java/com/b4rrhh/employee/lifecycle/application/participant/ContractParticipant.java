package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.application.port.HireParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeDependentRelationInvalidException;
import org.springframework.stereotype.Component;

@Component
public class ContractParticipant implements HireParticipant {

    private final CreateContractUseCase createContractUseCase;

    public ContractParticipant(CreateContractUseCase createContractUseCase) {
        this.createContractUseCase = createContractUseCase;
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public void participate(HireContext ctx) {
        try {
            ctx.setContractResult(createContractUseCase.create(new CreateContractCommand(
                    ctx.ruleSystemCode(),
                    ctx.employeeTypeCode(),
                    ctx.employeeNumber(),
                    ctx.contract().contractTypeCode(),
                    ctx.contract().contractSubtypeCode(),
                    ctx.hireDate(),
                    null
            )));
        } catch (ContractInvalidException | ContractSubtypeInvalidException ex) {
            throw new HireEmployeeCatalogValueInvalidException(ex.getMessage(), ex);
        } catch (ContractSubtypeRelationInvalidException ex) {
            throw new HireEmployeeDependentRelationInvalidException(ex.getMessage(), ex);
        }
    }
}
