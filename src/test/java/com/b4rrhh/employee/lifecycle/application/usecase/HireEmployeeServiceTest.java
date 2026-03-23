package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeCommand;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeConflictException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeDependentRelationInvalidException;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
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
class HireEmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private CreateEmployeeUseCase createEmployeeUseCase;
    @Mock
    private CreatePresenceUseCase createPresenceUseCase;
    @Mock
    private CreateLaborClassificationUseCase createLaborClassificationUseCase;
    @Mock
    private CreateContractUseCase createContractUseCase;
    @Mock
    private CreateWorkCenterUseCase createWorkCenterUseCase;
        @Mock
        private ListEmployeePresencesUseCase listEmployeePresencesUseCase;
        @Mock
        private ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase;
        @Mock
        private ListEmployeeContractsUseCase listEmployeeContractsUseCase;
        @Mock
        private ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase;

    private HireEmployeeService service;

    @BeforeEach
    void setUp() {
        service = new HireEmployeeService(
                employeeRepository,
                createEmployeeUseCase,
                createPresenceUseCase,
                createLaborClassificationUseCase,
                createContractUseCase,
                                createWorkCenterUseCase,
                new HireEmployeeCurrentStateReader(
                        listEmployeePresencesUseCase,
                        listEmployeeLaborClassificationsUseCase,
                        listEmployeeContractsUseCase,
                        listEmployeeWorkCentersUseCase
                ),
                new HireEmployeeIdempotencyEvaluator()
        );
    }

    @Test
    void hiresEmployeeAndPropagatesHireDateToAllInitialRecords() {
        HireEmployeeCommand command = validCommand();
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        100L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "Ana",
                        "Lopez",
                        null,
                        "Ani",
                        "ACTIVE",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenReturn(new Presence(
                        10L,
                        100L,
                        1,
                        "COMP",
                        "HIRE",
                        null,
                        LocalDate.of(2026, 3, 23),
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class)))
                .thenReturn(new WorkCenter(
                        20L,
                        100L,
                        1,
                        "WC1",
                        LocalDate.of(2026, 3, 23),
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));

        HireEmployeeResult result = service.hire(command);

        assertEquals("ESP", result.ruleSystemCode());
        assertEquals("INTERNAL", result.employeeTypeCode());
        assertEquals("EMP001", result.employeeNumber());
        assertEquals(LocalDate.of(2026, 3, 23), result.hireDate());
        assertEquals(1, result.presenceNumber());
        assertEquals("CON", result.contractTypeCode());
        assertEquals(1, result.workCenterAssignmentNumber());
        assertEquals(true, result.created());

        ArgumentCaptor<CreatePresenceCommand> presenceCaptor = ArgumentCaptor.forClass(CreatePresenceCommand.class);
        verify(createPresenceUseCase).create(presenceCaptor.capture());
        assertEquals(LocalDate.of(2026, 3, 23), presenceCaptor.getValue().startDate());

        ArgumentCaptor<CreateLaborClassificationCommand> laborCaptor = ArgumentCaptor.forClass(CreateLaborClassificationCommand.class);
        verify(createLaborClassificationUseCase).create(laborCaptor.capture());
        assertEquals(LocalDate.of(2026, 3, 23), laborCaptor.getValue().startDate());

        ArgumentCaptor<CreateContractCommand> contractCaptor = ArgumentCaptor.forClass(CreateContractCommand.class);
        verify(createContractUseCase).create(contractCaptor.capture());
        assertEquals(LocalDate.of(2026, 3, 23), contractCaptor.getValue().startDate());

        ArgumentCaptor<CreateWorkCenterCommand> workCenterCaptor = ArgumentCaptor.forClass(CreateWorkCenterCommand.class);
        verify(createWorkCenterUseCase).create(workCenterCaptor.capture());
        assertEquals(LocalDate.of(2026, 3, 23), workCenterCaptor.getValue().startDate());
    }

    @Test
    void returnsIdempotentResultWhenEmployeeAlreadyExistsWithEquivalentInitialState() {
        HireEmployeeCommand command = validCommand();
        Employee existingEmployee = existingEmployee();
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(existingEmployee));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence()));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLaborClassification()));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(activeContract()));
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter()));

        HireEmployeeResult result = service.hire(command);

        assertEquals(false, result.created());
        verify(createEmployeeUseCase, never()).create(any(CreateEmployeeCommand.class));
        verify(createPresenceUseCase, never()).create(any(CreatePresenceCommand.class));
    }

    @Test
    void failsWithConflictWhenEmployeeAlreadyExistsWithNonEquivalentState() {
        HireEmployeeCommand command = validCommand();
        Employee existingEmployee = existingEmployee();

        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(existingEmployee));
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence()));
        when(listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(any(ListEmployeeLaborClassificationsCommand.class)))
                .thenReturn(List.of(activeLaborClassification()));
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of());
        when(listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeWorkCenter()));

        assertThrows(HireEmployeeConflictException.class, () -> service.hire(command));
        verify(createEmployeeUseCase, never()).create(any(CreateEmployeeCommand.class));
    }

    @Test
    void mapsInvalidCatalogValueToLifecycleException() {
        HireEmployeeCommand command = validCommand();
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        1L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "Ana",
                        "Lopez",
                        null,
                        null,
                        "ACTIVE",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenThrow(new PresenceCatalogValueInvalidException("companyCode", "BAD"));

        assertThrows(HireEmployeeCatalogValueInvalidException.class, () -> service.hire(command));
        verify(createLaborClassificationUseCase, never()).create(any(CreateLaborClassificationCommand.class));
    }

    @Test
    void mapsInvalidAgreementCategoryDependencyToLifecycleException() {
        HireEmployeeCommand command = validCommand();
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        1L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "Ana",
                        "Lopez",
                        null,
                        null,
                        "ACTIVE",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenThrow(new LaborClassificationAgreementCategoryRelationInvalidException(
                        "ESP",
                        "AGR",
                        "CAT",
                        LocalDate.of(2026, 3, 23)
                ));

        assertThrows(HireEmployeeDependentRelationInvalidException.class, () -> service.hire(command));
        verify(createContractUseCase, never()).create(any(CreateContractCommand.class));
    }

    @Test
    void mapsInvalidContractSubtypeDependencyToLifecycleException() {
        HireEmployeeCommand command = validCommand();
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        1L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "Ana",
                        "Lopez",
                        null,
                        null,
                        "ACTIVE",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenThrow(new ContractSubtypeRelationInvalidException(
                        "ESP",
                        "CON",
                        "SUB",
                        LocalDate.of(2026, 3, 23)
                ));

        assertThrows(HireEmployeeDependentRelationInvalidException.class, () -> service.hire(command));
        verify(createWorkCenterUseCase, never()).create(any(CreateWorkCenterCommand.class));
    }

    private HireEmployeeCommand validCommand() {
        return new HireEmployeeCommand(
                "esp",
                "internal",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                LocalDate.of(2026, 3, 23),
                "comp",
                "hire",
                "agr",
                "cat",
                "con",
                "sub",
                "wc1"
        );
    }

        private Employee existingEmployee() {
                return new Employee(
                                1L,
                                "ESP",
                                "INTERNAL",
                                "EMP001",
                                "Ana",
                                "Lopez",
                                null,
                                "Ani",
                                "ACTIVE",
                                LocalDateTime.now(),
                                LocalDateTime.now()
                );
        }

        private Presence activePresence() {
                return new Presence(
                                10L,
                                1L,
                                1,
                                "COMP",
                                "HIRE",
                                null,
                                LocalDate.of(2026, 3, 23),
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                );
        }

        private LaborClassification activeLaborClassification() {
                return new LaborClassification(
                                1L,
                                "AGR",
                                "CAT",
                                LocalDate.of(2026, 3, 23),
                                null
                );
        }

        private Contract activeContract() {
                return new Contract(
                                1L,
                                "CON",
                                "SUB",
                                LocalDate.of(2026, 3, 23),
                                null
                );
        }

        private WorkCenter activeWorkCenter() {
                return new WorkCenter(
                                20L,
                                1L,
                                1,
                                "WC1",
                                LocalDate.of(2026, 3, 23),
                                null,
                                LocalDateTime.now(),
                                LocalDateTime.now()
                );
        }
}
