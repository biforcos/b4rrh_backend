package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.employee.presence.domain.model.Presence;
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
class PresenceParticipantTest {

    @Mock
    private CreatePresenceUseCase createPresenceUseCase;

    @InjectMocks
    private PresenceParticipant participant;

    @Test
    void orderIs20() {
        assertThat(participant.order()).isEqualTo(20);
    }

    @Test
    void createsPresenceFromContextAndStoresResult() {
        HireContext ctx = validContext();
        LocalDate hireDate = ctx.hireDate();
        LocalDateTime fixedNow = LocalDateTime.of(2026, 3, 23, 0, 0);
        Presence presence = new Presence(10L, 100L, 1, "COMP", "HIRE", null, hireDate, null,
                fixedNow, fixedNow);
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class))).thenReturn(presence);

        participant.participate(ctx);

        assertThat(ctx.presence()).isSameAs(presence);

        ArgumentCaptor<CreatePresenceCommand> captor = ArgumentCaptor.forClass(CreatePresenceCommand.class);
        verify(createPresenceUseCase).create(captor.capture());
        CreatePresenceCommand cmd = captor.getValue();
        assertThat(cmd.ruleSystemCode()).isEqualTo("ESP");
        assertThat(cmd.employeeTypeCode()).isEqualTo("INTERNAL");
        assertThat(cmd.employeeNumber()).isEqualTo("EMP000001");
        assertThat(cmd.companyCode()).isEqualTo("COMP");
        assertThat(cmd.entryReasonCode()).isEqualTo("HIRE");
        assertThat(cmd.exitReasonCode()).isNull();
        assertThat(cmd.startDate()).isEqualTo(hireDate);
        assertThat(cmd.endDate()).isNull();
    }

    @Test
    void wrapsPresenceCatalogValueInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenThrow(new PresenceCatalogValueInvalidException("companyCode", "BAD"));

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
