package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCategoryInvalidException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
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
class LaborClassificationParticipantTest {

    @Mock
    private CreateLaborClassificationUseCase createLaborClassificationUseCase;

    @InjectMocks
    private LaborClassificationParticipant participant;

    @Test
    void orderIs60() {
        assertThat(participant.order()).isEqualTo(60);
    }

    @Test
    void createsLaborClassificationFromContextAndStoresResult() {
        HireContext ctx = validContext();
        LocalDate hireDate = ctx.hireDate();
        LaborClassification laborClassification = new LaborClassification(100L, "AGR", "CAT", hireDate, null);
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenReturn(laborClassification);

        participant.participate(ctx);

        assertThat(ctx.laborClassificationResult()).isSameAs(laborClassification);

        ArgumentCaptor<CreateLaborClassificationCommand> captor =
                ArgumentCaptor.forClass(CreateLaborClassificationCommand.class);
        verify(createLaborClassificationUseCase).create(captor.capture());
        CreateLaborClassificationCommand cmd = captor.getValue();
        assertThat(cmd.ruleSystemCode()).isEqualTo("ESP");
        assertThat(cmd.employeeTypeCode()).isEqualTo("INTERNAL");
        assertThat(cmd.employeeNumber()).isEqualTo("EMP000001");
        assertThat(cmd.agreementCode()).isEqualTo("AGR");
        assertThat(cmd.agreementCategoryCode()).isEqualTo("CAT");
        assertThat(cmd.startDate()).isEqualTo(hireDate);
        assertThat(cmd.endDate()).isNull();
    }

    @Test
    void wrapsAgreementInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenThrow(new LaborClassificationAgreementInvalidException("BAD_AGR"));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeCatalogValueInvalidException.class);
    }

    @Test
    void wrapsAgreementCategoryRelationInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenThrow(new LaborClassificationAgreementCategoryRelationInvalidException(
                        "ESP", "AGR", "BAD_CAT", LocalDate.of(2026, 3, 23)));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeDependentRelationInvalidException.class);
    }

    @Test
    void wrapsCategoryInvalidExceptionToLifecycleException() {
        HireContext ctx = validContext();
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenThrow(new LaborClassificationCategoryInvalidException("BAD_CAT"));

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
