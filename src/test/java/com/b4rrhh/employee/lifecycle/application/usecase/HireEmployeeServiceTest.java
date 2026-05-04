package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterDistributionUseCase;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeCommand;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeAlreadyExistsException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeBusinessValidationException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeConflictException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeDependentRelationInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeRequestInvalidException;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.working_time.application.usecase.CreateWorkingTimeCommand;
import com.b4rrhh.employee.working_time.application.usecase.CreateWorkingTimeUseCase;
import com.b4rrhh.employee.working_time.domain.exception.InvalidWorkingTimePercentageException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNumberConflictException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCompanyMismatchException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterCompanyLookupPort;
import com.b4rrhh.employee.workcenter.domain.service.WorkCenterCompanyValidator;
import com.b4rrhh.employee.employee.application.service.EmployeeTypeCatalogValidator;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
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
    private CreateCostCenterDistributionUseCase createCostCenterDistributionUseCase;
    @Mock
    private CreateWorkingTimeUseCase createWorkingTimeUseCase;
    @Mock
    private WorkCenterCompanyLookupPort workCenterCompanyLookupPort;
    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private WorkCenterCompanyValidator workCenterCompanyValidator;
    private EmployeeTypeCatalogValidator employeeTypeCatalogValidator;
    private HireEmployeeService service;

    @BeforeEach
    void setUp() {
        workCenterCompanyValidator = new WorkCenterCompanyValidator(workCenterCompanyLookupPort);
        employeeTypeCatalogValidator = new EmployeeTypeCatalogValidator(ruleEntityRepository);
        RuleEntity validEmployeeType = new RuleEntity(
                1L, "ESP", "EMPLOYEE_TYPE", "INTERNAL", "Internal", null, true,
                LocalDate.of(2000, 1, 1), null, LocalDateTime.now(), LocalDateTime.now()
        );
        lenient().when(ruleEntityRepository.findByBusinessKey("ESP", "EMPLOYEE_TYPE", "INTERNAL"))
                .thenReturn(Optional.of(validEmployeeType));
        service = new HireEmployeeService(
                employeeRepository,
                createEmployeeUseCase,
                createPresenceUseCase,
                createLaborClassificationUseCase,
                createContractUseCase,
                createWorkCenterUseCase,
                createCostCenterDistributionUseCase,
                createWorkingTimeUseCase,
                workCenterCompanyValidator,
                employeeTypeCatalogValidator
        );
    }

    @Test
    void hiresEmployeeAndPropagatesHireDateToAllInitialRecords() {
        HireEmployeeCommand command = validCommand();
        LocalDate hireDate = command.hireDate();
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "WC1", hireDate)).thenReturn(Optional.of("COMP"));

        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        100L, "ESP", "INTERNAL", "EMP001", "Ana", "Lopez", null, "Ani", "ACTIVE",
                        LocalDateTime.now(), LocalDateTime.now(), null
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenReturn(new Presence(
                        10L, 100L, 1, "COMP", "HIRE", null, hireDate, null,
                        LocalDateTime.now(), LocalDateTime.now()
                ));
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenReturn(new LaborClassification(100L, "AGR", "CAT", hireDate, null));
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenReturn(new Contract(100L, "CON", "SUB", hireDate, null));
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class)))
                .thenReturn(new WorkCenter(
                        20L, 100L, 1, "WC1", hireDate, null,
                        LocalDateTime.now(), LocalDateTime.now()
                ));
        when(createWorkingTimeUseCase.create(any(CreateWorkingTimeCommand.class)))
                .thenReturn(workingTime(100L, 1, hireDate, null, new BigDecimal("75")));

        HireEmployeeResult result = service.hire(command);

        assertEquals("ESP", result.employee().ruleSystemCode());
        assertEquals("INTERNAL", result.employee().employeeTypeCode());
        assertEquals("EMP001", result.employee().employeeNumber());
        assertEquals(hireDate, result.presence().startDate());
        assertEquals(1, result.presence().presenceNumber());
        assertEquals("CON", result.contract().contractTypeCode());
        assertEquals(1, result.workingTime().workingTimeNumber());
        assertEquals(hireDate, result.workingTime().startDate());
        assertNull(result.workingTime().endDate());
        assertEquals(new BigDecimal("75"), result.workingTime().workingTimePercentage());
        assertEquals(new BigDecimal("30.00"), result.workingTime().weeklyHours());
        assertEquals(new BigDecimal("6.00"), result.workingTime().dailyHours());
        assertEquals(new BigDecimal("125.00"), result.workingTime().monthlyHours());

        ArgumentCaptor<CreatePresenceCommand> presenceCaptor = ArgumentCaptor.forClass(CreatePresenceCommand.class);
        verify(createPresenceUseCase).create(presenceCaptor.capture());
        assertEquals(hireDate, presenceCaptor.getValue().startDate());

        ArgumentCaptor<CreateLaborClassificationCommand> laborCaptor = ArgumentCaptor.forClass(CreateLaborClassificationCommand.class);
        verify(createLaborClassificationUseCase).create(laborCaptor.capture());
        assertEquals(hireDate, laborCaptor.getValue().startDate());

        ArgumentCaptor<CreateContractCommand> contractCaptor = ArgumentCaptor.forClass(CreateContractCommand.class);
        verify(createContractUseCase).create(contractCaptor.capture());
        assertEquals(hireDate, contractCaptor.getValue().startDate());

        ArgumentCaptor<CreateWorkCenterCommand> workCenterCaptor = ArgumentCaptor.forClass(CreateWorkCenterCommand.class);
        verify(createWorkCenterUseCase).create(workCenterCaptor.capture());
        assertEquals(hireDate, workCenterCaptor.getValue().startDate());

        ArgumentCaptor<CreateWorkingTimeCommand> workingTimeCaptor = ArgumentCaptor.forClass(CreateWorkingTimeCommand.class);
        verify(createWorkingTimeUseCase).create(workingTimeCaptor.capture());
        assertEquals("ESP", workingTimeCaptor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", workingTimeCaptor.getValue().employeeTypeCode());
        assertEquals("EMP001", workingTimeCaptor.getValue().employeeNumber());
        assertEquals(hireDate, workingTimeCaptor.getValue().startDate());
        assertEquals(new BigDecimal("75"), workingTimeCaptor.getValue().workingTimePercentage());
    }

    @Test
    void failsFastWhenWorkCenterDoesNotBelongToCompany() {
        HireEmployeeCommand command = validCommand();

        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "WC1", LocalDate.of(2026, 3, 23)))
                .thenReturn(Optional.of("OTHER"));

        assertThrows(WorkCenterCompanyMismatchException.class, () -> service.hire(command));
        verify(createEmployeeUseCase, never()).create(any(CreateEmployeeCommand.class));
    }

    @Test
    void failsWhenEmployeeAlreadyExists() {
        HireEmployeeCommand command = validCommand();
        Employee existingEmployee = new Employee(
                1L, "ESP", "INTERNAL", "EMP001", "Ana", "Lopez", null, "Ani", "ACTIVE",
                LocalDateTime.now(), LocalDateTime.now(), null
        );

        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(existingEmployee));

        assertThrows(HireEmployeeAlreadyExistsException.class, () -> service.hire(command));
        verify(createEmployeeUseCase, never()).create(any(CreateEmployeeCommand.class));
    }

    @Test
    void mapsInvalidCatalogValueToLifecycleException() {
        HireEmployeeCommand command = validCommand();
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "WC1", LocalDate.of(2026, 3, 23))).thenReturn(Optional.of("COMP"));
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        1L, "ESP", "INTERNAL", "EMP001", "Ana", "Lopez", null, null, "ACTIVE",
                        LocalDateTime.now(), LocalDateTime.now(), null
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenThrow(new PresenceCatalogValueInvalidException("companyCode", "BAD"));

        assertThrows(HireEmployeeCatalogValueInvalidException.class, () -> service.hire(command));
    }

    @Test
    void mapsInvalidAgreementCategoryDependencyToLifecycleException() {
        HireEmployeeCommand command = validCommand();
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "WC1", LocalDate.of(2026, 3, 23))).thenReturn(Optional.of("COMP"));
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        1L, "ESP", "INTERNAL", "EMP001", "Ana", "Lopez", null, null, "ACTIVE",
                        LocalDateTime.now(), LocalDateTime.now(), null
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenReturn(new Presence(1L, 1L, 1, "C", "R", null, LocalDate.now(), null, LocalDateTime.now(), LocalDateTime.now()));
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenThrow(new LaborClassificationAgreementCategoryRelationInvalidException(
                        "ESP", "AGR", "CAT", LocalDate.of(2026, 3, 23)
                ));

        assertThrows(HireEmployeeDependentRelationInvalidException.class, () -> service.hire(command));
    }

    @Test
    void failsWhenWorkingTimeIsMissing() {
        HireEmployeeCommand command = new HireEmployeeCommand(
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                LocalDate.of(2026, 3, 23),
                "HIRE",
                "COMP",
                "WC1",
                new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
                new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
                null,
                null
        );

        assertThrows(HireEmployeeRequestInvalidException.class, () -> service.hire(command));
    }

    @Test
    void failsWhenContractIsMissing() {
        HireEmployeeCommand command = new HireEmployeeCommand(
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                LocalDate.of(2026, 3, 23),
                "HIRE",
                "COMP",
                "WC1",
                null,
                new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
                null,
                new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(new BigDecimal("75"))
        );

        HireEmployeeRequestInvalidException exception =
                assertThrows(HireEmployeeRequestInvalidException.class, () -> service.hire(command));

        assertEquals("contract is required", exception.getMessage());
    }

    @Test
    void failsWhenLaborClassificationIsMissing() {
        HireEmployeeCommand command = new HireEmployeeCommand(
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                LocalDate.of(2026, 3, 23),
                "HIRE",
                "COMP",
                "WC1",
                new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
                null,
                null,
                new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(new BigDecimal("75"))
        );

        HireEmployeeRequestInvalidException exception =
                assertThrows(HireEmployeeRequestInvalidException.class, () -> service.hire(command));

        assertEquals("laborClassification is required", exception.getMessage());
    }

    @Test
    void failsWhenWorkingTimePercentageIsMissing() {
        HireEmployeeCommand command = new HireEmployeeCommand(
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                LocalDate.of(2026, 3, 23),
                "HIRE",
                "COMP",
                "WC1",
                new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
                new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
                null,
                new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(null)
        );

        assertThrows(HireEmployeeRequestInvalidException.class, () -> service.hire(command));
    }

    @Test
    void mapsInvalidWorkingTimePercentageToBusinessValidationException() {
        HireEmployeeCommand command = validCommand();
        LocalDate hireDate = command.hireDate();
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "WC1", hireDate)).thenReturn(Optional.of("COMP"));
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        100L, "ESP", "INTERNAL", "EMP001", "Ana", "Lopez", null, "Ani", "ACTIVE",
                        LocalDateTime.now(), LocalDateTime.now(), null
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenReturn(new Presence(
                        10L, 100L, 1, "COMP", "HIRE", null, hireDate, null,
                        LocalDateTime.now(), LocalDateTime.now()
                ));
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenReturn(new LaborClassification(100L, "AGR", "CAT", hireDate, null));
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenReturn(new Contract(100L, "CON", "SUB", hireDate, null));
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class)))
                .thenReturn(new WorkCenter(
                        20L, 100L, 1, "WC1", hireDate, null,
                        LocalDateTime.now(), LocalDateTime.now()
                ));
        when(createWorkingTimeUseCase.create(any(CreateWorkingTimeCommand.class)))
                .thenThrow(new InvalidWorkingTimePercentageException("workingTimePercentage must be greater than 0 and less than or equal to 100"));

        assertThrows(HireEmployeeBusinessValidationException.class, () -> service.hire(command));
    }

    @Test
    void mapsWorkingTimeNumberConflictToLifecycleConflictException() {
        HireEmployeeCommand command = validCommand();
        LocalDate hireDate = command.hireDate();
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "WC1", hireDate)).thenReturn(Optional.of("COMP"));
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(createEmployeeUseCase.create(any(CreateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        100L, "ESP", "INTERNAL", "EMP001", "Ana", "Lopez", null, "Ani", "ACTIVE",
                        LocalDateTime.now(), LocalDateTime.now(), null
                ));
        when(createPresenceUseCase.create(any(CreatePresenceCommand.class)))
                .thenReturn(new Presence(
                        10L, 100L, 1, "COMP", "HIRE", null, hireDate, null,
                        LocalDateTime.now(), LocalDateTime.now()
                ));
        when(createLaborClassificationUseCase.create(any(CreateLaborClassificationCommand.class)))
                .thenReturn(new LaborClassification(100L, "AGR", "CAT", hireDate, null));
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenReturn(new Contract(100L, "CON", "SUB", hireDate, null));
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class)))
                .thenReturn(new WorkCenter(
                        20L, 100L, 1, "WC1", hireDate, null,
                        LocalDateTime.now(), LocalDateTime.now()
                ));
        when(createWorkingTimeUseCase.create(any(CreateWorkingTimeCommand.class)))
                .thenThrow(new WorkingTimeNumberConflictException(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        1,
                        new RuntimeException("duplicate working time number")
                ));

        assertThrows(HireEmployeeConflictException.class, () -> service.hire(command));
    }

    private HireEmployeeCommand validCommand() {
        return new HireEmployeeCommand(
                "ESP",
                "INTERNAL",
                "EMP001",
                "Ana",
                "Lopez",
                null,
                "Ani",
                LocalDate.of(2026, 3, 23),
                "HIRE",
                "COMP",
                "WC1",
                new HireEmployeeCommand.HireEmployeeContractCommand("CON", "SUB"),
                new HireEmployeeCommand.HireEmployeeLaborClassificationCommand("AGR", "CAT"),
                null,
                new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(new BigDecimal("75"))
        );
    }

    @Test
    void mapsInvalidEmployeeTypeToLifecycleException() {
        HireEmployeeCommand command = validCommand();
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "WC1", command.hireDate()))
                .thenReturn(Optional.of("COMP"));
        when(employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.empty());
        when(ruleEntityRepository.findByBusinessKey("ESP", "EMPLOYEE_TYPE", "INTERNAL"))
                .thenReturn(Optional.empty());

        assertThrows(HireEmployeeCatalogValueInvalidException.class, () -> service.hire(command));
        verify(createEmployeeUseCase, never()).create(any(CreateEmployeeCommand.class));
    }

    private WorkingTime workingTime(Long employeeId, Integer workingTimeNumber, LocalDate startDate, LocalDate endDate, BigDecimal percentage) {
        return WorkingTime.rehydrate(
                30L,
                employeeId,
                workingTimeNumber,
                startDate,
                endDate,
                percentage,
                new WorkingTimeDerivedHours(new BigDecimal("30.00"), new BigDecimal("6.00"), new BigDecimal("125.00")),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
