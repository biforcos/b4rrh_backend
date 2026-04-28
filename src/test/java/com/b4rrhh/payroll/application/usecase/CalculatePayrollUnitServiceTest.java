package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.application.port.AgreementProfileLookupPort;
import com.b4rrhh.payroll.application.port.CompanyProfileLookupPort;
import com.b4rrhh.payroll.application.port.EmployeePersonalDataLookupPort;
import com.b4rrhh.payroll.application.port.EmployeePayrollInputLookupPort;
import com.b4rrhh.payroll.application.port.WorkCenterProfileLookupPort;
import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputLookupPort;
import com.b4rrhh.payroll.application.port.PayrollLaunchWorkingTimeWindowContext;
import com.b4rrhh.payroll.application.service.PayrollConceptExecutionResult;
import com.b4rrhh.payroll.application.service.PayrollConceptGraphCalculator;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import com.b4rrhh.payroll.infrastructure.config.PayrollLaunchExecutionProperties;
import com.b4rrhh.payroll_engine.concept.domain.model.CalculationType;
import com.b4rrhh.payroll_engine.concept.domain.model.ExecutionScope;
import com.b4rrhh.payroll_engine.concept.domain.model.FunctionalNature;
import com.b4rrhh.payroll_engine.concept.domain.model.ResultCompositionMode;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.planning.application.service.BuildEligibleExecutionPlanUseCase;
import com.b4rrhh.payroll_engine.planning.domain.model.EligibleExecutionPlanResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculatePayrollUnitServiceTest {

    @Mock
    private CalculatePayrollUseCase calculatePayrollUseCase;
    @Mock
    private PayrollLaunchEligibleInputLookupPort payrollLaunchEligibleInputLookupPort;
    @Mock
    private PayrollConceptGraphCalculator payrollConceptGraphCalculator;
    @Mock
    private BuildEligibleExecutionPlanUseCase buildEligibleExecutionPlanUseCase;
    @Mock
    private CompanyProfileLookupPort companyProfileLookupPort;
    @Mock
    private EmployeePersonalDataLookupPort employeePersonalDataLookupPort;
    @Mock
    private AgreementProfileLookupPort agreementProfileLookupPort;
    @Mock
    private WorkCenterProfileLookupPort workCenterProfileLookupPort;
    @Mock
    private EmployeePayrollInputLookupPort employeePayrollInputLookupPort;
    @Test
        void generatesDeterministicFakeConceptsAndSnapshotForInternalLaunchCalculation() {
        PayrollLaunchExecutionProperties properties = new PayrollLaunchExecutionProperties();
        properties.setMode(PayrollExecutionMode.FAKE);

        CalculatePayrollUnitService service = new CalculatePayrollUnitService(
            calculatePayrollUseCase,
            payrollLaunchEligibleInputLookupPort,
            properties,
            payrollConceptGraphCalculator,
            buildEligibleExecutionPlanUseCase,
            companyProfileLookupPort,
            employeePersonalDataLookupPort,
            agreementProfileLookupPort,
            workCenterProfileLookupPort,
            List.of(),
            employeePayrollInputLookupPort
        );
        when(calculatePayrollUseCase.calculate(org.mockito.ArgumentMatchers.any(CalculatePayrollCommand.class)))
                .thenReturn(payroll());

        service.calculate(new CalculatePayrollUnitCommand(
                "ESP",
                "INTERNAL",
                "EMP001",
                "202501",
                "ORD",
                2,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 31),
                "ENGINE",
                "1.0"
        ));

        ArgumentCaptor<CalculatePayrollCommand> captor = ArgumentCaptor.forClass(CalculatePayrollCommand.class);
        verify(calculatePayrollUseCase).calculate(captor.capture());

        CalculatePayrollCommand command = captor.getValue();
        assertEquals(PayrollStatus.CALCULATED, command.status());
        assertEquals(10, command.concepts().size());
        assertEquals("BASE_FAKE", command.concepts().get(0).getConceptCode());
        assertEquals("SENIORITY_FAKE", command.concepts().get(1).getConceptCode());
        assertEquals("TRANSPORT_FAKE", command.concepts().get(2).getConceptCode());
        assertEquals("BONUS_FAKE", command.concepts().get(3).getConceptCode());
        assertEquals("OVERTIME_FAKE", command.concepts().get(4).getConceptCode());
        assertEquals("GROSS_FAKE", command.concepts().get(5).getConceptCode());
        assertEquals("TAX_FAKE", command.concepts().get(6).getConceptCode());
        assertEquals("SS_FAKE", command.concepts().get(7).getConceptCode());
        assertEquals("DEDUCTION_FAKE", command.concepts().get(8).getConceptCode());
        assertEquals("NET_FAKE", command.concepts().get(9).getConceptCode());
        assertEquals(1, command.warnings().size());
        assertEquals("DETERMINISTIC_FAKE_PAYROLL", command.warnings().getFirst().warningCode());
        assertEquals("INFO", command.warnings().getFirst().severityCode());
        assertEquals("Payroll generated by deterministic fake calculator", command.warnings().getFirst().message());
        assertTrue(command.warnings().getFirst().detailsJson().contains("DETERMINISTIC_FAKE"));
        assertEquals(1, command.contextSnapshots().size());
        assertEquals("EMPLOYEE_PAYROLL_CONTEXT", command.contextSnapshots().get(0).getSnapshotTypeCode());
        assertEquals("PAYROLL_LAUNCH", command.contextSnapshots().get(0).getSourceVerticalCode());
        assertTrue(command.contextSnapshots().get(0).getSourceBusinessKeyJson().contains("EMP001"));
        assertTrue(command.contextSnapshots().get(0).getSnapshotPayloadJson().contains("DETERMINISTIC_FAKE"));
        verifyNoInteractions(payrollConceptGraphCalculator);
        }

        @Test
        void eligibleRealMode_persistsSingleConcept101FromMinimalExecutor() {
        PayrollLaunchExecutionProperties properties = new PayrollLaunchExecutionProperties();
        properties.setMode(PayrollExecutionMode.ELIGIBLE_REAL);

        CalculatePayrollUnitService service = new CalculatePayrollUnitService(
            calculatePayrollUseCase,
            payrollLaunchEligibleInputLookupPort,
            properties,
            payrollConceptGraphCalculator,
            buildEligibleExecutionPlanUseCase,
            companyProfileLookupPort,
            employeePersonalDataLookupPort,
            agreementProfileLookupPort,
            workCenterProfileLookupPort,
            List.of(),
            employeePayrollInputLookupPort
        );

        when(payrollLaunchEligibleInputLookupPort.findByUnitAndPeriod(
            "ESP", "INTERNAL", "EMP001", 2,
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
        )).thenReturn(Optional.of(new PayrollLaunchEligibleInputContext(
            "ES01",
                        "99002405011982",
                        "99002405-G2",
            List.of(new PayrollLaunchWorkingTimeWindowContext(
                LocalDate.of(2025, 1, 1),
                null,
                new BigDecimal("100")
            )),
            LocalDate.of(2025, 1, 1),
            null,
            null
        )));

        when(payrollConceptGraphCalculator.calculateConceptResult(org.mockito.ArgumentMatchers.eq("101"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PayrollConceptExecutionResult(
                        "101",
                        new BigDecimal("1425.00"),
                        new BigDecimal("30"),
                        new BigDecimal("47.50")
                ));
        when(calculatePayrollUseCase.calculate(org.mockito.ArgumentMatchers.any(CalculatePayrollCommand.class)))
            .thenReturn(payroll());

        PayrollObject obj101 = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.CONCEPT, "101", null, null);
        com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept engineConcept101 =
                new com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept(
                        obj101, "SALARIO_BASE", CalculationType.DIRECT_AMOUNT, FunctionalNature.EARNING,
                        ResultCompositionMode.REPLACE, "101", ExecutionScope.PERIOD, null, null);
        ConceptExecutionPlanEntry entry101 = new ConceptExecutionPlanEntry(
                new ConceptNodeIdentity("ESP", "101"), CalculationType.DIRECT_AMOUNT);
        EligibleExecutionPlanResult planResult = new EligibleExecutionPlanResult(
                List.of(), List.of(), List.of(engineConcept101), null, List.of(entry101));
        when(buildEligibleExecutionPlanUseCase.build(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(planResult);

        service.calculate(new CalculatePayrollUnitCommand(
            "ESP",
            "INTERNAL",
            "EMP001",
            "202501",
            "ORD",
            2,
            LocalDate.of(2025, 1, 1),
            LocalDate.of(2025, 1, 31),
            "ENGINE",
            "1.0"
        ));

        ArgumentCaptor<CalculatePayrollCommand> captor = ArgumentCaptor.forClass(CalculatePayrollCommand.class);
        verify(calculatePayrollUseCase).calculate(captor.capture());

        CalculatePayrollCommand persisted = captor.getValue();
        assertEquals(1, persisted.concepts().size());
        assertEquals("101", persisted.concepts().getFirst().getConceptCode());
        assertEquals(0, new BigDecimal("1425.00").compareTo(persisted.concepts().getFirst().getAmount()));
        assertEquals(0, new BigDecimal("30").compareTo(persisted.concepts().getFirst().getQuantity()));
        assertEquals(0, new BigDecimal("47.50").compareTo(persisted.concepts().getFirst().getRate()));
        verify(payrollConceptGraphCalculator, never()).calculateConceptResult(org.mockito.ArgumentMatchers.eq("D01"), org.mockito.ArgumentMatchers.any());
        verify(payrollConceptGraphCalculator, never()).calculateConceptResult(org.mockito.ArgumentMatchers.eq("P01"), org.mockito.ArgumentMatchers.any());
        }

        @Test
        void eligibleRealMode_missingAgreementCategory_throwsExplicitException() {
        PayrollLaunchExecutionProperties properties = new PayrollLaunchExecutionProperties();
        properties.setMode(PayrollExecutionMode.ELIGIBLE_REAL);

        CalculatePayrollUnitService service = new CalculatePayrollUnitService(
            calculatePayrollUseCase,
            payrollLaunchEligibleInputLookupPort,
            properties,
            payrollConceptGraphCalculator,
            buildEligibleExecutionPlanUseCase,
            companyProfileLookupPort,
            employeePersonalDataLookupPort,
            agreementProfileLookupPort,
            workCenterProfileLookupPort,
            List.of(),
            employeePayrollInputLookupPort
        );

        when(payrollLaunchEligibleInputLookupPort.findByUnitAndPeriod(
            "ESP", "INTERNAL", "EMP001", 2,
            LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31)
        )).thenReturn(Optional.of(new PayrollLaunchEligibleInputContext(
            "ES01",
                        "99002405011982",
            null,
            List.of(new PayrollLaunchWorkingTimeWindowContext(
                LocalDate.of(2025, 1, 1),
                null,
                new BigDecimal("100")
            )),
            LocalDate.of(2025, 1, 1),
            null,
            null
        )));

        PayrollLaunchInputMissingException ex = assertThrows(PayrollLaunchInputMissingException.class, () ->
            service.calculate(new CalculatePayrollUnitCommand(
                "ESP",
                "INTERNAL",
                "EMP001",
                "202501",
                "ORD",
                2,
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 1, 31),
                "ENGINE",
                "1.0"
            ))
        );

        assertEquals("AGREEMENT_CATEGORY_MISSING", ex.getReasonCode());
        verifyNoInteractions(payrollConceptGraphCalculator);
    }

    private Payroll payroll() {
        return Payroll.rehydrate(
                1L,
                "ESP",
                "INTERNAL",
                "EMP001",
                "202501",
                "ORD",
                2,
                PayrollStatus.CALCULATED,
                null,
                LocalDateTime.of(2026, 4, 11, 10, 0),
                "ENGINE",
                "1.0",
                List.of(),
                List.of(),
                LocalDateTime.of(2026, 4, 11, 10, 0),
                LocalDateTime.of(2026, 4, 11, 10, 0)
        );
    }
}