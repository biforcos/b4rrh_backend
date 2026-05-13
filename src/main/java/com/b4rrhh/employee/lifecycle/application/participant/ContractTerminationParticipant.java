package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.usecase.CloseContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;
import com.b4rrhh.employee.lifecycle.application.port.TerminationParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractTerminationParticipant implements TerminationParticipant {

    private final ListEmployeeContractsUseCase listContracts;
    private final CloseContractUseCase closeContract;

    public ContractTerminationParticipant(
            ListEmployeeContractsUseCase listContracts,
            CloseContractUseCase closeContract) {
        this.listContracts = listContracts;
        this.closeContract = closeContract;
    }

    @Override
    public int order() { return 40; }

    @Override
    public void participate(TerminationContext ctx) {
        List<Contract> all = listContracts.listByEmployeeBusinessKey(
                new ListEmployeeContractsCommand(
                        ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber()));

        List<Contract> active = all.stream()
                .filter(c -> c.getEndDate() == null)
                .toList();

        if (active.size() > 1) {
            throw new TerminateEmployeeConflictException(
                    "Multiple active contracts found for employee " + ctx.employeeNumber());
        }
        if (active.isEmpty()) return;

        Contract activeContract = active.get(0);
        if (activeContract.getStartDate().isAfter(ctx.terminationDate())) return;

        try {
            Contract closed = closeContract.close(new CloseContractCommand(
                    ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                    activeContract.getStartDate(), ctx.terminationDate()));
            ctx.setClosedContract(closed);
        } catch (ContractInvalidException | ContractSubtypeInvalidException e) {
            throw new TerminateEmployeeCatalogValueInvalidException(e.getMessage(), e);
        } catch (RuntimeException e) {
            throw new TerminateEmployeeConflictException(e.getMessage(), e);
        }
    }
}
