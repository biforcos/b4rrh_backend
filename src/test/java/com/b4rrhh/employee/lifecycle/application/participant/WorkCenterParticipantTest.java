package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkCenterParticipantTest {

    @Mock
    private CreateWorkCenterUseCase createWorkCenterUseCase;

    @InjectMocks
    private WorkCenterParticipant participant;

    @Test
    void orderIs30() {
        assertThat(participant.order()).isEqualTo(30);
    }

    @Test
    void createsWorkCenterFromContextAndStoresResult() {
        HireContext ctx = validContext();
        LocalDate hireDate = ctx.hireDate();
        LocalDateTime fixedNow = LocalDateTime.of(2026, 3, 23, 0, 0);
        WorkCenter workCenter = new WorkCenter(20L, 100L, 1, "WC1", hireDate, null, fixedNow, fixedNow);
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class))).thenReturn(workCenter);

        participant.participate(ctx);

        assertThat(ctx.workCenter()).isSameAs(workCenter);

        ArgumentCaptor<CreateWorkCenterCommand> captor = ArgumentCaptor.forClass(CreateWorkCenterCommand.class);
        verify(createWorkCenterUseCase).create(captor.capture());
        CreateWorkCenterCommand cmd = captor.getValue();
        assertThat(cmd.ruleSystemCode()).isEqualTo("ESP");
        assertThat(cmd.employeeTypeCode()).isEqualTo("INTERNAL");
        assertThat(cmd.employeeNumber()).isEqualTo("EMP000001");
        assertThat(cmd.workCenterCode()).isEqualTo("WC1");
        assertThat(cmd.startDate()).isEqualTo(hireDate);
        assertThat(cmd.endDate()).isNull();
    }

    @Test
    void wrapsWorkCenterCatalogValueInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class)))
                .thenThrow(new WorkCenterCatalogValueInvalidException("workCenterCode", "BAD"));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeCatalogValueInvalidException.class);
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
