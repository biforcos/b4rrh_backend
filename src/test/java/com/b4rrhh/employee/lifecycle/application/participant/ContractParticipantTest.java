package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeDependentRelationInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractParticipantTest {

    @Mock
    private CreateContractUseCase createContractUseCase;

    @InjectMocks
    private ContractParticipant participant;

    @Test
    void orderIs50() {
        assertThat(participant.order()).isEqualTo(50);
    }

    @Test
    void createsContractFromContextAndStoresResult() {
        HireContext ctx = validContext();
        LocalDate hireDate = ctx.hireDate();
        Contract contract = new Contract(100L, "CON", "SUB", hireDate, null);
        when(createContractUseCase.create(any(CreateContractCommand.class))).thenReturn(contract);

        participant.participate(ctx);

        assertThat(ctx.contractResult()).isSameAs(contract);

        ArgumentCaptor<CreateContractCommand> captor = ArgumentCaptor.forClass(CreateContractCommand.class);
        verify(createContractUseCase).create(captor.capture());
        CreateContractCommand cmd = captor.getValue();
        assertThat(cmd.ruleSystemCode()).isEqualTo("ESP");
        assertThat(cmd.employeeTypeCode()).isEqualTo("INTERNAL");
        assertThat(cmd.employeeNumber()).isEqualTo("EMP000001");
        assertThat(cmd.contractCode()).isEqualTo("CON");
        assertThat(cmd.contractSubtypeCode()).isEqualTo("SUB");
        assertThat(cmd.startDate()).isEqualTo(hireDate);
        assertThat(cmd.endDate()).isNull();
    }

    @Test
    void wrapsContractInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenThrow(new ContractInvalidException("CON"));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeCatalogValueInvalidException.class);
    }

    @Test
    void wrapsContractSubtypeInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenThrow(new ContractSubtypeInvalidException("SUB"));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeCatalogValueInvalidException.class);
    }

    @Test
    void wrapsContractSubtypeRelationInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        LocalDate hireDate = ctx.hireDate();
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenThrow(new ContractSubtypeRelationInvalidException("ESP", "CON", "SUB", hireDate));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeDependentRelationInvalidException.class);
    }

    private HireContext validContext() {
        HireContext ctx = new HireContext(
                "ESP", "INTERNAL", "Ana", "Lopez", null, "Ani",
                LocalDate.of(2026, 3, 23),
                "COMP", "HIRE", "WC1",
                new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
                new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
                null,
                new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(new BigDecimal("75"))
        );
        ctx.setEmployeeNumber("EMP000001");
        return ctx;
    }
}
