package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.usecase.CloseContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.employee.application.usecase.GetEmployeeByBusinessKeyUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CloseLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminateEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TerminateEmployeeServiceTest {

    @Mock
    private GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private ListEmployeePresencesUseCase listEmployeePresencesUseCase;
    @Mock
    private ListEmployeeContractsUseCase listEmployeeContractsUseCase;
    @Mock
    private ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase;
    @Mock
    private ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase;
    @Mock
    private CloseWorkCenterUseCase closeWorkCenterUseCase;
    @Mock
    private CloseLaborClassificationUseCase closeLaborClassificationUseCase;
    @Mock
    private CloseContractUseCase closeContractUseCase;
    @Mock
    private ClosePresenceUseCase closePresenceUseCase;

    private TerminateEmployeeService service;

    @BeforeEach
    void setUp() {
        service = new TerminateEmployeeService(
                getEmployeeByBusinessKeyUseCase,
                employeeRepository,
                listEmployeePresencesUseCase,
                listEmployeeContractsUseCase,
                listEmployeeLaborClassificationsUseCase,
                listEmployeeWorkCentersUseCase,
                closeWorkCenterUseCase,
                closeLaborClassificationUseCase,
                closeContractUseCase,
                closePresenceUseCase
        );
    }

    @Test
    void terminatesEmployeeAndClosesAllActiveOccurrencesWithSingleTerminationDate() {
        TerminateEmployeeCommand command = validCommand();

        Employee existing = employee("ACTIVE");
        Employee terminated = employee("TERMINATED");

        Presence activePresence = activePresence();
        Contract activeContract = activeContract();
        LaborClassification activeLaborClassification = activeLaborClassification();
        WorkCenter activeWorkCenter = activeWorkCenter();

        Presence closedPresence = new Presence(
                10L,
                100L,
                1,
                "COMP",
                "HIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Contract closedContract = new Contract(
                100L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );

        LaborClassification closedLaborClassification = new LaborClassification(
                100L,
                "AGR",
                "CAT",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );

        WorkCenter closedWorkCenter = new WorkCenter(
                20L,
                100L,
                1,
                "WC1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(existing));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence), List.of(closedPresence));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLaborClassification));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter));
        when(closeWorkCenterUseCase.close(any(CloseWorkCenterCommand.class))).thenReturn(closedWorkCenter);
        when(closeLaborClassificationUseCase.close(any(CloseLaborClassificationCommand.class))).thenReturn(closedLaborClassification);
        when(closeContractUseCase.close(any(CloseContractCommand.class))).thenReturn(closedContract);
        when(closePresenceUseCase.close(any(ClosePresenceCommand.class))).thenReturn(closedPresence);
        when(employeeRepository.save(any(Employee.class))).thenReturn(terminated);

        TerminateEmployeeResult result = service.terminate(command);

        assertEquals("ESP", result.ruleSystemCode());
        assertEquals("INTERNAL", result.employeeTypeCode());
        assertEquals("EMP001", result.employeeNumber());
        assertEquals(LocalDate.of(2026, 3, 31), result.terminationDate());
        assertEquals("VOL", result.exitReasonCode());
        assertEquals("TERMINATED", result.status());
        assertEquals(1, result.closedPresenceNumber());
        assertEquals("IND", result.closedContractTypeCode());
        assertEquals("AGR", result.closedAgreementCode());
        assertEquals(1, result.closedWorkCenterAssignmentNumber());

        InOrder inOrder = inOrder(
                closeWorkCenterUseCase,
                closeLaborClassificationUseCase,
                closeContractUseCase,
                closePresenceUseCase,
                employeeRepository,
                listEmployeePresencesUseCase
        );
        inOrder.verify(closeWorkCenterUseCase).close(any(CloseWorkCenterCommand.class));
        inOrder.verify(closeLaborClassificationUseCase).close(any(CloseLaborClassificationCommand.class));
        inOrder.verify(closeContractUseCase).close(any(CloseContractCommand.class));
        inOrder.verify(closePresenceUseCase).close(any(ClosePresenceCommand.class));
        inOrder.verify(employeeRepository).save(any(Employee.class));

        ArgumentCaptor<ClosePresenceCommand> closePresenceCaptor = ArgumentCaptor.forClass(ClosePresenceCommand.class);
        verify(closePresenceUseCase).close(closePresenceCaptor.capture());
        assertEquals(LocalDate.of(2026, 3, 31), closePresenceCaptor.getValue().endDate());
        assertEquals("VOL", closePresenceCaptor.getValue().exitReasonCode());
    }

    @Test
    void failsWhenThereIsNoActivePresence() {
        TerminateEmployeeCommand command = validCommand();
        Presence closedPresence = new Presence(
                10L,
                100L,
                1,
                "COMP",
                "HIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(closedPresence));

        assertThrows(TerminateEmployeeConflictException.class, () -> service.terminate(command));
        verify(closePresenceUseCase, never()).close(any(ClosePresenceCommand.class));
    }

    @Test
    void failsWhenThereAreMultipleActivePresences() {
        TerminateEmployeeCommand command = validCommand();

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence(), activePresence()));

        assertThrows(TerminateEmployeeConflictException.class, () -> service.terminate(command));
        verify(closePresenceUseCase, never()).close(any(ClosePresenceCommand.class));
    }

    @Test
    void mapsInvalidExitReasonCatalogToTerminateCatalogException() {
        TerminateEmployeeCommand command = validCommand();

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence()));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract()));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLaborClassification()));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter()));
        when(closeWorkCenterUseCase.close(any(CloseWorkCenterCommand.class))).thenReturn(activeWorkCenter());
        when(closeLaborClassificationUseCase.close(any(CloseLaborClassificationCommand.class))).thenReturn(activeLaborClassification());
        when(closeContractUseCase.close(any(CloseContractCommand.class))).thenReturn(activeContract());
        when(closePresenceUseCase.close(any(ClosePresenceCommand.class)))
                .thenThrow(new PresenceCatalogValueInvalidException("exitReasonCode", "VOL"));

        assertThrows(TerminateEmployeeCatalogValueInvalidException.class, () -> service.terminate(command));
    }

    @Test
    void returnsIdempotentSuccessWhenEmployeeAlreadyTerminatedWithSameDateAndExitReason() {
        TerminateEmployeeCommand command = validCommand();
        Employee alreadyTerminated = employee("TERMINATED");

        Presence closedPresence = new Presence(
                10L,
                100L,
                1,
                "COMP",
                "HIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Contract closedContract = new Contract(
                100L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );
        LaborClassification closedLabor = new LaborClassification(
                100L,
                "AGR",
                "CAT",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31)
        );
        WorkCenter closedWorkCenter = new WorkCenter(
                20L,
                100L,
                1,
                "WC1",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(alreadyTerminated));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(closedPresence));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(closedContract));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(closedLabor));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(closedWorkCenter));

        TerminateEmployeeResult result = service.terminate(command);

        assertEquals("TERMINATED", result.status());
        assertEquals(LocalDate.of(2026, 3, 31), result.terminationDate());
        assertEquals("VOL", result.exitReasonCode());
        verify(closeWorkCenterUseCase, never()).close(any(CloseWorkCenterCommand.class));
        verify(closeLaborClassificationUseCase, never()).close(any(CloseLaborClassificationCommand.class));
        verify(closeContractUseCase, never()).close(any(CloseContractCommand.class));
        verify(closePresenceUseCase, never()).close(any(ClosePresenceCommand.class));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void failsWhenEmployeeAlreadyTerminatedWithDifferentDate() {
        TerminateEmployeeCommand command = validCommand();
        Employee alreadyTerminated = employee("TERMINATED");

        Presence closedPresenceDifferentDate = new Presence(
                10L,
                100L,
                1,
                "COMP",
                "HIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 2, 28),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(alreadyTerminated));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(closedPresenceDifferentDate));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of());

        assertThrows(TerminateEmployeeConflictException.class, () -> service.terminate(command));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void doesNotCloseFutureOptionalOccurrence() {
        TerminateEmployeeCommand command = validCommand();

        Presence activePresence = activePresence();
        Presence closedPresence = new Presence(
                10L,
                100L,
                1,
                "COMP",
                "HIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 3, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Contract futureContract = new Contract(
                100L,
                "IND",
                "FT1",
                LocalDate.of(2026, 4, 1),
                null
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence), List.of(closedPresence));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(futureContract));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of());
        when(closePresenceUseCase.close(any(ClosePresenceCommand.class))).thenReturn(closedPresence);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee("TERMINATED"));

        TerminateEmployeeResult result = service.terminate(command);

        assertEquals("TERMINATED", result.status());
        assertEquals(1, result.closedPresenceNumber());
        assertEquals(null, result.closedContractTypeCode());
        verify(closeContractUseCase, never()).close(any(CloseContractCommand.class));
        verify(closeLaborClassificationUseCase, never()).close(any(CloseLaborClassificationCommand.class));
        verify(closeWorkCenterUseCase, never()).close(any(CloseWorkCenterCommand.class));
    }

    @Test
    void failsWhenThereAreMultipleActiveContracts() {
        TerminateEmployeeCommand command = validCommand();

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence()));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract(), activeContract()));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLaborClassification()));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter()));

        assertThrows(TerminateEmployeeConflictException.class, () -> service.terminate(command));
        verify(closeContractUseCase, never()).close(any(CloseContractCommand.class));
    }

    @Test
    void failsWhenTerminationDateIsBeforeActivePresenceStartDate() {
        TerminateEmployeeCommand command = new TerminateEmployeeCommand(
                "esp",
                "internal",
                "EMP001",
                LocalDate.of(2025, 12, 31),
                "vol"
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence()));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract()));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLaborClassification()));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter()));

        assertThrows(TerminateEmployeeConflictException.class, () -> service.terminate(command));
        verify(closePresenceUseCase, never()).close(any(ClosePresenceCommand.class));
    }

    private TerminateEmployeeCommand validCommand() {
        return new TerminateEmployeeCommand(
                "esp",
                "internal",
                "EMP001",
                LocalDate.of(2026, 3, 31),
                "vol"
        );
    }

    private Employee employee(String status) {
        return new Employee(
                100L,
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                status,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Presence activePresence() {
        return new Presence(
                10L,
                100L,
                1,
                "COMP",
                "HIRE",
                null,
                LocalDate.of(2026, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Contract activeContract() {
        return new Contract(
                100L,
                "IND",
                "FT1",
                LocalDate.of(2026, 1, 1),
                null
        );
    }

    private LaborClassification activeLaborClassification() {
        return new LaborClassification(
                100L,
                "AGR",
                "CAT",
                LocalDate.of(2026, 1, 1),
                null
        );
    }

    private WorkCenter activeWorkCenter() {
        return new WorkCenter(
                20L,
                100L,
                1,
                "WC1",
                LocalDate.of(2026, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
