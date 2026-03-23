package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.employee.application.usecase.GetEmployeeByBusinessKeyUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.command.RehireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.RehireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.RehireEmployeeConflictException;
import com.b4rrhh.employee.lifecycle.domain.exception.RehireEmployeeEmployeeNotFoundException;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RehireEmployeeServiceTest {

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
    private CreatePresenceUseCase createPresenceUseCase;
    @Mock
    private CreateLaborClassificationUseCase createLaborClassificationUseCase;
    @Mock
    private CreateContractUseCase createContractUseCase;
    @Mock
    private CreateWorkCenterUseCase createWorkCenterUseCase;

    private RehireEmployeeService service;

    @BeforeEach
    void setUp() {
        service = new RehireEmployeeService(
                getEmployeeByBusinessKeyUseCase,
                employeeRepository,
                listEmployeePresencesUseCase,
                listEmployeeContractsUseCase,
                listEmployeeLaborClassificationsUseCase,
                listEmployeeWorkCentersUseCase,
                createPresenceUseCase,
                createLaborClassificationUseCase,
                createContractUseCase,
                createWorkCenterUseCase
        );
    }

    @Test
    void rehiresEmployeeAndPropagatesRehireDateToAllInitialRecords() {
        RehireEmployeeCommand command = validCommand();

        Presence closedPresence = closedPresence(LocalDate.of(2026, 3, 31));
        Presence newPresence = activePresence(LocalDate.of(2026, 4, 15));
        LaborClassification newLabor = activeLabor(LocalDate.of(2026, 4, 15));
        Contract newContract = activeContract(LocalDate.of(2026, 4, 15));
        WorkCenter newWorkCenter = activeWorkCenter(LocalDate.of(2026, 4, 15));

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("TERMINATED")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(closedPresence), List.of(newPresence));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(new Contract(1L, "CON", "SUB", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(new LaborClassification(1L, "AGR", "CAT", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31))));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(new WorkCenter(10L, 1L, 1, "WC1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 3, 31), LocalDateTime.now(), LocalDateTime.now())));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class))).thenReturn(newPresence);
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class))).thenReturn(newLabor);
        when(createContractUseCase.create(any(CreateContractCommand.class))).thenReturn(newContract);
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class))).thenReturn(newWorkCenter);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee("ACTIVE"));

        RehireEmployeeResult result = service.rehire(command);

        assertEquals("ACTIVE", result.status());
        assertEquals(LocalDate.of(2026, 4, 15), result.rehireDate());
        assertEquals(1, result.newPresenceNumber());
        assertEquals("CON", result.newContractTypeCode());
        assertEquals("METAL", result.newAgreementCode());
        assertEquals(1, result.newWorkCenterAssignmentNumber());
        assertEquals(true, result.created());

        ArgumentCaptor<CreatePresenceCommand> presenceCaptor = ArgumentCaptor.forClass(CreatePresenceCommand.class);
        verify(createPresenceUseCase).create(presenceCaptor.capture());
        assertEquals(LocalDate.of(2026, 4, 15), presenceCaptor.getValue().startDate());

        ArgumentCaptor<CreateLaborClassificationCommand> laborCaptor = ArgumentCaptor.forClass(CreateLaborClassificationCommand.class);
        verify(createLaborClassificationUseCase).create(laborCaptor.capture());
        assertEquals(LocalDate.of(2026, 4, 15), laborCaptor.getValue().startDate());

        ArgumentCaptor<CreateContractCommand> contractCaptor = ArgumentCaptor.forClass(CreateContractCommand.class);
        verify(createContractUseCase).create(contractCaptor.capture());
        assertEquals(LocalDate.of(2026, 4, 15), contractCaptor.getValue().startDate());

        ArgumentCaptor<CreateWorkCenterCommand> workCenterCaptor = ArgumentCaptor.forClass(CreateWorkCenterCommand.class);
        verify(createWorkCenterUseCase).create(workCenterCaptor.capture());
        assertEquals(LocalDate.of(2026, 4, 15), workCenterCaptor.getValue().startDate());
    }

    @Test
    void failsWhenEmployeeNotFound() {
        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());

        assertThrows(RehireEmployeeEmployeeNotFoundException.class, () -> service.rehire(validCommand()));
        verify(createPresenceUseCase, never()).create(any(CreatePresenceCommand.class));
    }

    @Test
    void failsIfActivePresenceAlreadyExists() {
        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("TERMINATED")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence(LocalDate.of(2026, 4, 15))));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract(LocalDate.of(2026, 4, 15))));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLabor(LocalDate.of(2026, 4, 15))));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter(LocalDate.of(2026, 4, 15))));

        assertThrows(RehireEmployeeConflictException.class, () -> service.rehire(validCommand()));
        verify(createPresenceUseCase, never()).create(any(CreatePresenceCommand.class));
    }

    @Test
    void failsIfNoPreviousClosedPresenceExists() {
        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("TERMINATED")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of());
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of());

        assertThrows(RehireEmployeeConflictException.class, () -> service.rehire(validCommand()));
        verify(createPresenceUseCase, never()).create(any(CreatePresenceCommand.class));
    }

    @Test
    void failsIfRehireDateIsNotAfterLastClosedPresenceEndDate() {
        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("TERMINATED")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(closedPresence(LocalDate.of(2026, 4, 15))));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of());

        assertThrows(RehireEmployeeConflictException.class, () -> service.rehire(validCommand()));
        verify(createPresenceUseCase, never()).create(any(CreatePresenceCommand.class));
    }

    @Test
    void returnsIdempotentSuccessWhenActiveCycleIsEquivalent() {
        Presence activePresence = activePresence(LocalDate.of(2026, 4, 15));

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract(LocalDate.of(2026, 4, 15))));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLabor(LocalDate.of(2026, 4, 15))));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter(LocalDate.of(2026, 4, 15))));

        RehireEmployeeResult result = service.rehire(validCommand());

        assertEquals("ACTIVE", result.status());
        assertEquals(1, result.newPresenceNumber());
                assertEquals(false, result.created());
        verify(createPresenceUseCase, never()).create(any(CreatePresenceCommand.class));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void failsWithConflictWhenActiveCycleIsNotEquivalent() {
        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("ACTIVE")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence(LocalDate.of(2026, 4, 15))));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract(LocalDate.of(2026, 4, 15))));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLabor(LocalDate.of(2026, 4, 15))));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(new WorkCenter(
                        20L,
                        100L,
                        1,
                        "WC9",
                        LocalDate.of(2026, 4, 15),
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                )));

        assertThrows(RehireEmployeeConflictException.class, () -> service.rehire(validCommand()));
    }

    @Test
    void rehiresUsingLatestClosedPresenceAsTemporalReferenceAcrossMultiplePreviousCycles() {
        RehireEmployeeCommand command = new RehireEmployeeCommand(
                "esp",
                "internal",
                "EMP001",
                LocalDate.of(2026, 6, 1),
                "rehire",
                "es01",
                "metal",
                "oficial_1",
                "con",
                "sub",
                "madrid_01"
        );

        Presence firstCycleClosed = new Presence(
                10L,
                100L,
                1,
                "ES01",
                "HIRE",
                "VOL",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Presence secondCycleClosed = new Presence(
                11L,
                100L,
                2,
                "ES01",
                "REHIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 5, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Presence thirdCycleActive = new Presence(
                12L,
                100L,
                3,
                "ES01",
                "REHIRE",
                null,
                LocalDate.of(2026, 6, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("TERMINATED")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(firstCycleClosed, secondCycleClosed), List.of(thirdCycleActive));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(
                        new Contract(100L, "CON", "SUB", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                        new Contract(100L, "CON", "SUB", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31))
                ));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(
                        new LaborClassification(100L, "METAL", "OFICIAL_1", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31)),
                        new LaborClassification(100L, "METAL", "OFICIAL_1", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31))
                ));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(
                        new WorkCenter(30L, 100L, 1, "MADRID_01", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), LocalDateTime.now(), LocalDateTime.now()),
                        new WorkCenter(31L, 100L, 2, "MADRID_01", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 5, 31), LocalDateTime.now(), LocalDateTime.now())
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class))).thenReturn(thirdCycleActive);
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class))).thenReturn(activeLabor(LocalDate.of(2026, 6, 1)));
        when(createContractUseCase.create(any(CreateContractCommand.class))).thenReturn(activeContract(LocalDate.of(2026, 6, 1)));
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class))).thenReturn(new WorkCenter(
                32L,
                100L,
                3,
                "MADRID_01",
                LocalDate.of(2026, 6, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        ));
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee("ACTIVE"));

        RehireEmployeeResult result = service.rehire(command);

        assertEquals(LocalDate.of(2026, 6, 1), result.rehireDate());
        assertEquals(3, result.newPresenceNumber());
        assertEquals(3, result.newWorkCenterAssignmentNumber());
        assertEquals(true, result.created());
    }

    @Test
    void failsWhenRehireDateIsAfterOlderCycleButNotAfterLatestClosedPresence() {
        RehireEmployeeCommand command = new RehireEmployeeCommand(
                "esp",
                "internal",
                "EMP001",
                LocalDate.of(2026, 4, 1),
                "rehire",
                "es01",
                "metal",
                "oficial_1",
                "con",
                "sub",
                "madrid_01"
        );

        Presence olderClosed = new Presence(
                10L,
                100L,
                1,
                "ES01",
                "HIRE",
                "VOL",
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        Presence latestClosed = new Presence(
                11L,
                100L,
                2,
                "ES01",
                "REHIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 5, 31),
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(getEmployeeByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(employee("TERMINATED")));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(olderClosed, latestClosed));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of());

        assertThrows(RehireEmployeeConflictException.class, () -> service.rehire(command));
        verify(createPresenceUseCase, never()).create(any(CreatePresenceCommand.class));
    }

    private RehireEmployeeCommand validCommand() {
        return new RehireEmployeeCommand(
                "esp",
                "internal",
                "EMP001",
                LocalDate.of(2026, 4, 15),
                "rehire",
                "es01",
                "metal",
                "oficial_1",
                "con",
                "sub",
                "madrid_01"
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

    private Presence closedPresence(LocalDate endDate) {
        return new Presence(
                10L,
                100L,
                1,
                "ES01",
                "HIRE",
                "VOL",
                LocalDate.of(2026, 1, 1),
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Presence activePresence(LocalDate startDate) {
        return new Presence(
                10L,
                100L,
                1,
                "ES01",
                "REHIRE",
                null,
                startDate,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Contract activeContract(LocalDate startDate) {
        return new Contract(
                100L,
                                "CON",
                                "SUB",
                startDate,
                null
        );
    }

    private LaborClassification activeLabor(LocalDate startDate) {
        return new LaborClassification(
                100L,
                "METAL",
                "OFICIAL_1",
                startDate,
                null
        );
    }

    private WorkCenter activeWorkCenter(LocalDate startDate) {
        return new WorkCenter(
                20L,
                100L,
                1,
                "MADRID_01",
                startDate,
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
