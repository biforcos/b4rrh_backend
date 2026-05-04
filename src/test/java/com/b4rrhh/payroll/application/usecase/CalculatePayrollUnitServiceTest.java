package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.application.port.AgreementProfileLookupPort;
import com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase.GetAgreementCategoryProfileQuery;
import com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase.GetAgreementCategoryProfileUseCase;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
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
import com.b4rrhh.payroll_engine.execution.application.service.SegmentExecutionEngine;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.SegmentExecutionState;
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
    @Mock
    private GetAgreementCategoryProfileUseCase getAgreementCategoryProfileUseCase;
    @Mock
    private SegmentExecutionEngine segmentExecutionEngine;

    @Test
        void eligibleRealMode_persistsSingleConcept101FromMinimalExecutor() {
        PayrollLaunchExecutionProperties properties = new PayrollLaunchExecutionProperties();

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
            segmentExecutionEngine,
            employeePayrollInputLookupPort,
            getAgreementCategoryProfileUseCase
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

        // Pre-computation of DIRECT_AMOUNT during the pre-compute pass
        when(payrollConceptGraphCalculator.calculateConceptResult(org.mockito.ArgumentMatchers.eq("101"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PayrollConceptExecutionResult(
                        "101",
                        new BigDecimal("1425.00"),
                        null,
                        null
                ));
        when(calculatePayrollUseCase.calculate(org.mockito.ArgumentMatchers.any(CalculatePayrollCommand.class)))
            .thenReturn(payroll());

        PayrollObject obj101 = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.CONCEPT, "101", null, null);
        com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept engineConcept101 =
                new com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept(
                        obj101, "SALARIO_BASE", CalculationType.DIRECT_AMOUNT, FunctionalNature.EARNING,
                        ResultCompositionMode.REPLACE, "101", ExecutionScope.PERIOD, true, null, null);
        ConceptExecutionPlanEntry entry101 = new ConceptExecutionPlanEntry(
                new ConceptNodeIdentity("ESP", "101"), CalculationType.DIRECT_AMOUNT);
        EligibleExecutionPlanResult planResult = new EligibleExecutionPlanResult(
                List.of(), List.of(), List.of(engineConcept101), null, List.of(entry101));
        when(buildEligibleExecutionPlanUseCase.build(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(planResult);
        when(getAgreementCategoryProfileUseCase.get(
                new GetAgreementCategoryProfileQuery("ESP", "99002405-G2")))
                .thenReturn(new AgreementCategoryProfile("05", TipoNomina.MENSUAL));

        // segmentExecutionEngine returns a state with concept 101 pre-computed amount
        SegmentExecutionState segmentState = new SegmentExecutionState();
        segmentState.storeResult(new ConceptNodeIdentity("ESP", "101"), new BigDecimal("1425.00"));
        when(segmentExecutionEngine.execute(
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any()))
                .thenReturn(segmentState);

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
        // quantity and rate are null for DIRECT_AMOUNT concepts
        verify(payrollConceptGraphCalculator, never()).calculateConceptResult(org.mockito.ArgumentMatchers.eq("D01"), org.mockito.ArgumentMatchers.any());
        verify(payrollConceptGraphCalculator, never()).calculateConceptResult(org.mockito.ArgumentMatchers.eq("P01"), org.mockito.ArgumentMatchers.any());
        }

        @Test
        void eligibleRealMode_missingAgreementCategory_throwsExplicitException() {
        PayrollLaunchExecutionProperties properties = new PayrollLaunchExecutionProperties();

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
            segmentExecutionEngine,
            employeePayrollInputLookupPort,
            getAgreementCategoryProfileUseCase
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
