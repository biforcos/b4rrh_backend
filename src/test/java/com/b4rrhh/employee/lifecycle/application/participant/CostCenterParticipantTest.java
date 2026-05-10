package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterDistributionCommand;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterDistributionUseCase;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CostCenterParticipantTest {

    @Mock
    private CreateCostCenterDistributionUseCase createCostCenterDistributionUseCase;

    @InjectMocks
    private CostCenterParticipant participant;

    @Test
    void orderIs40() {
        assertThat(participant.order()).isEqualTo(40);
    }

    @Test
    void skipsCreationWhenCostCenterDistributionIsNull() {
        HireContext ctx = contextWithoutCostCenter();

        participant.participate(ctx);

        verify(createCostCenterDistributionUseCase, never()).create(any());
        assertThat(ctx.costCenter()).isNull();
    }

    @Test
    void createsCostCenterDistributionWhenPresent() {
        HireContext ctx = contextWithCostCenter();
        CostCenterDistributionWindow window = mock(CostCenterDistributionWindow.class);
        when(createCostCenterDistributionUseCase.create(any(CreateCostCenterDistributionCommand.class)))
                .thenReturn(window);

        participant.participate(ctx);

        assertThat(ctx.costCenter()).isSameAs(window);
    }

    @Test
    void wrapsCostCenterCatalogValueInvalidExceptionToLifecycleException() {
        HireContext ctx = contextWithCostCenter();
        when(createCostCenterDistributionUseCase.create(any(CreateCostCenterDistributionCommand.class)))
                .thenThrow(new CostCenterCatalogValueInvalidException("costCenterCode", "BAD"));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeCatalogValueInvalidException.class);
    }

    @Test
    void wrapsCostCenterDistributionInvalidExceptionToLifecycleException() {
        HireContext ctx = contextWithCostCenter();
        when(createCostCenterDistributionUseCase.create(any(CreateCostCenterDistributionCommand.class)))
                .thenThrow(new CostCenterDistributionInvalidException("Invalid distribution"));

        assertThatThrownBy(() -> participant.participate(ctx))
                .isInstanceOf(HireEmployeeCatalogValueInvalidException.class);
    }

    private HireContext contextWithoutCostCenter() {
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

    private HireContext contextWithCostCenter() {
        HireContext ctx = new HireContext(
                "ESP", "INTERNAL", "Ana", "Lopez", null, "Ani",
                LocalDate.of(2026, 3, 23),
                "COMP", "HIRE", "WC1",
                new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
                new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
                new HireEmployeeCommand.HireEmployeeCostCenterDistributionCommand(
                        List.of(new HireEmployeeCommand.HireEmployeeCostCenterItemCommand("CC1", 100.0))
                ),
                new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(new BigDecimal("75"))
        );
        ctx.setEmployeeNumber("EMP000001");
        return ctx;
    }
}
