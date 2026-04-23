package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputLookupPort;
import com.b4rrhh.payroll.application.port.PayrollLaunchWorkingTimeWindowContext;
import com.b4rrhh.payroll.application.service.RealPayrollConceptLinesCalculator;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollConcept;
import com.b4rrhh.payroll.domain.model.PayrollContextSnapshot;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import com.b4rrhh.payroll.domain.model.PayrollWarning;
import com.b4rrhh.payroll.infrastructure.config.PayrollLaunchExecutionProperties;
import com.b4rrhh.payroll_engine.planning.application.service.EligiblePayrollExecutionRequest;
import com.b4rrhh.payroll_engine.planning.application.service.EligiblePayrollExecutionResult;
import com.b4rrhh.payroll_engine.planning.application.service.ExecuteEligiblePayrollUseCase;
import com.b4rrhh.payroll_engine.segment.domain.model.WorkingTimeWindow;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CalculatePayrollUnitService implements CalculatePayrollUnitUseCase {

    private final CalculatePayrollUseCase calculatePayrollUseCase;
    private final ExecuteEligiblePayrollUseCase executeEligiblePayrollUseCase;
    private final PayrollLaunchEligibleInputLookupPort payrollLaunchEligibleInputLookupPort;
    private final PayrollLaunchExecutionProperties payrollLaunchExecutionProperties;
    private final RealPayrollConceptLinesCalculator calculateRealPayrollConceptLinesService;

    public CalculatePayrollUnitService(
            CalculatePayrollUseCase calculatePayrollUseCase,
            ExecuteEligiblePayrollUseCase executeEligiblePayrollUseCase,
            PayrollLaunchEligibleInputLookupPort payrollLaunchEligibleInputLookupPort,
            PayrollLaunchExecutionProperties payrollLaunchExecutionProperties,
            RealPayrollConceptLinesCalculator calculateRealPayrollConceptLinesService
    ) {
        this.calculatePayrollUseCase = calculatePayrollUseCase;
        this.executeEligiblePayrollUseCase = executeEligiblePayrollUseCase;
        this.payrollLaunchEligibleInputLookupPort = payrollLaunchEligibleInputLookupPort;
        this.payrollLaunchExecutionProperties = payrollLaunchExecutionProperties;
        this.calculateRealPayrollConceptLinesService = calculateRealPayrollConceptLinesService;
    }

    @Override
    public Payroll calculate(CalculatePayrollUnitCommand command) {
        return switch (payrollLaunchExecutionProperties.getMode()) {
            case ELIGIBLE_REAL -> calculateEligibleReal(command);
            case MINIMAL_REAL -> calculateMinimalReal(command);
            case FAKE -> calculateFake(command);
        };
    }

    private Payroll calculateFake(CalculatePayrollUnitCommand command) {
        return calculatePayrollUseCase.calculate(new CalculatePayrollCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.payrollPeriodCode(),
                command.payrollTypeCode(),
                command.presenceNumber(),
                PayrollStatus.CALCULATED,
                null,
                LocalDateTime.now(),
                command.calculationEngineCode(),
                command.calculationEngineVersion(),
                fakeWarnings(command),
                fakeConcepts(command),
                List.of(fakeSnapshot(command))
        ));
    }

            private Payroll calculateEligibleReal(CalculatePayrollUnitCommand command) {
            Optional<PayrollLaunchEligibleInputContext> inputOpt = payrollLaunchEligibleInputLookupPort.findByUnitAndPeriod(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.presenceNumber(),
                command.periodStart(),
                command.periodEnd()
            );

            if (inputOpt.isEmpty()) {
                throw new PayrollLaunchInputMissingException(
                    "ELIGIBLE_INPUT_CONTEXT_NOT_FOUND",
                    "Eligible real execution skipped: launcher input context is missing for payroll unit",
                    Map.of(
                        "executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name(),
                        "employeeTypeCode", command.employeeTypeCode(),
                        "employeeNumber", command.employeeNumber(),
                        "presenceNumber", command.presenceNumber()
                    )
                );
            }

            PayrollLaunchEligibleInputContext input = inputOpt.get();
            if (input.companyCode() == null || input.companyCode().isBlank()) {
                throw new PayrollLaunchInputMissingException(
                    "COMPANY_CODE_MISSING",
                    "Eligible real execution skipped: companyCode is required but missing in launcher context",
                    Map.of("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name())
                );
            }

            BigDecimal monthlySalaryAmount = payrollLaunchExecutionProperties.getEligibleRealMonthlySalaryAmount();
            if (monthlySalaryAmount == null) {
                throw new PayrollLaunchInputMissingException(
                    "MONTHLY_SALARY_NOT_CONFIGURED",
                    "Eligible real execution skipped: payroll.launch.execution.eligible-real-monthly-salary-amount is not configured",
                    Map.of("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name())
                );
            }

            List<WorkingTimeWindow> workingTimeWindows = input.workingTimeWindows().stream()
                .map(this::toWorkingTimeWindow)
                .toList();
            if (workingTimeWindows.isEmpty()) {
                throw new PayrollLaunchInputMissingException(
                    "WORKING_TIME_WINDOWS_MISSING",
                    "Eligible real execution skipped: no working time windows overlap payroll period",
                    Map.of("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name())
                );
            }

            EligiblePayrollExecutionResult eligibleResult = executeEligiblePayrollUseCase.execute(
                new EligiblePayrollExecutionRequest(
                    command.ruleSystemCode(),
                    command.employeeTypeCode(),
                    command.employeeNumber(),
                    input.companyCode(),
                    input.agreementCode(),
                    command.periodStart(),
                    command.periodEnd(),
                    monthlySalaryAmount,
                    workingTimeWindows
                )
            );

            return calculatePayrollUseCase.calculate(new CalculatePayrollCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.payrollPeriodCode(),
                command.payrollTypeCode(),
                command.presenceNumber(),
                PayrollStatus.CALCULATED,
                null,
                LocalDateTime.now(),
                command.calculationEngineCode(),
                command.calculationEngineVersion(),
                List.of(eligibleRealWarning(command, eligibleResult)),
                eligibleRealConcepts(command, eligibleResult),
                List.of(eligibleRealSnapshot(command, eligibleResult))
            ));
            }

            private WorkingTimeWindow toWorkingTimeWindow(PayrollLaunchWorkingTimeWindowContext window) {
            return new WorkingTimeWindow(window.startDate(), window.endDate(), window.workingTimePercentage());
            }

            private List<PayrollConcept> eligibleRealConcepts(
                CalculatePayrollUnitCommand command,
                EligiblePayrollExecutionResult result
            ) {
            return List.of(
                new PayrollConcept(1, "TOTAL_SALARIO_BASE", "Total salario base", result.getTotalSalarioBase(),
                    BigDecimal.ONE, result.getTotalSalarioBase(), "EARNING", command.payrollPeriodCode(), 1),
                new PayrollConcept(2, "TOTAL_PLUS_TRANSPORTE", "Total plus transporte", result.getTotalPlusTransporte(),
                    BigDecimal.ONE, result.getTotalPlusTransporte(), "EARNING", command.payrollPeriodCode(), 2),
                new PayrollConcept(3, "TOTAL_DEVENGOS_CONSOLIDATED", "Total devengos consolidated", result.getTotalDevengosConsolidated(),
                    BigDecimal.ONE, result.getTotalDevengosConsolidated(), "GROSS", command.payrollPeriodCode(), 3),
                new PayrollConcept(4, "TOTAL_RETENCION_IRPF", "Total retencion IRPF", result.getTotalRetencionIrpf().negate(),
                    BigDecimal.ONE, result.getTotalRetencionIrpf(), "DEDUCTION", command.payrollPeriodCode(), 4)
            );
            }

            private PayrollWarning eligibleRealWarning(
                CalculatePayrollUnitCommand command,
                EligiblePayrollExecutionResult result
            ) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name());
            details.put("employeeTypeCode", command.employeeTypeCode());
            details.put("employeeNumber", command.employeeNumber());
            details.put("totalSalarioBase", result.getTotalSalarioBase());
            details.put("totalPlusTransporte", result.getTotalPlusTransporte());
            details.put("totalDevengosConsolidated", result.getTotalDevengosConsolidated());
            details.put("totalRetencionIrpf", result.getTotalRetencionIrpf());
            details.put("segmentCount", result.getSegmentResults().size());
            return new PayrollWarning(
                null,
                null,
                "ELIGIBLE_REAL_EXECUTION",
                "INFO",
                "Payroll generated by eligible real payroll_engine execution",
                toJson(details)
            );
            }

            private PayrollContextSnapshot eligibleRealSnapshot(
                CalculatePayrollUnitCommand command,
                EligiblePayrollExecutionResult result
            ) {
            Map<String, Object> sourceKey = new LinkedHashMap<>();
            sourceKey.put("ruleSystemCode", command.ruleSystemCode());
            sourceKey.put("employeeTypeCode", command.employeeTypeCode());
            sourceKey.put("employeeNumber", command.employeeNumber());
            sourceKey.put("payrollPeriodCode", command.payrollPeriodCode());
            sourceKey.put("payrollTypeCode", command.payrollTypeCode());
            sourceKey.put("presenceNumber", command.presenceNumber());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name());
            payload.put("referenceDate", result.getReferenceDate().toString());
            payload.put("segmentCount", result.getSegmentResults().size());
            int eligibleConceptCount = result.getPlanningResult() == null
                ? 0
                : result.getPlanningResult().eligibleConcepts().size();
            int expandedConceptCount = result.getPlanningResult() == null
                ? 0
                : result.getPlanningResult().expandedConcepts().size();
            payload.put("eligibleConceptCount", eligibleConceptCount);
            payload.put("expandedConceptCount", expandedConceptCount);

            return new PayrollContextSnapshot(
                "EMPLOYEE_PAYROLL_CONTEXT",
                "PAYROLL_ENGINE",
                toJson(sourceKey),
                toJson(payload)
            );
            }

            private Payroll calculateMinimalReal(CalculatePayrollUnitCommand command) {
            // Calculate BASE_SALARY and PLUS_CONVENIO concepts
            List<PayrollConcept> minimalConcepts = calculateRealPayrollConceptLinesService.calculateConceptLines(command);

            // If no concepts were calculated (none activated), return empty list
            // This is a valid state — not all agreements have all concepts active
            if (minimalConcepts.isEmpty()) {
                minimalConcepts = List.of(); // Empty payroll allowed for minimal_real mode
            }

            return calculatePayrollUseCase.calculate(new CalculatePayrollCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.payrollPeriodCode(),
                command.payrollTypeCode(),
                command.presenceNumber(),
                PayrollStatus.CALCULATED,
                null,
                LocalDateTime.now(),
                command.calculationEngineCode(),
                command.calculationEngineVersion(),
                List.of(minimalRealWarning(command, minimalConcepts)),
                minimalConcepts,
                List.of(minimalRealSnapshot(command, minimalConcepts))
            ));
            }

            private PayrollWarning minimalRealWarning(CalculatePayrollUnitCommand command, List<PayrollConcept> concepts) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("executionMode", PayrollExecutionMode.MINIMAL_REAL.name());
            details.put("employeeTypeCode", command.employeeTypeCode());
            details.put("employeeNumber", command.employeeNumber());
            details.put("conceptCount", concepts.size());
            details.put("conceptCodes", concepts.stream().map(PayrollConcept::getConceptCode).toList());
            return new PayrollWarning(
                null,
                null,
                "MINIMAL_REAL_EXECUTION",
                "INFO",
                "Payroll generated by minimal real concept calculation (BASE_SALARY, PLUS_CONVENIO)",
                toJson(details)
            );
            }

            private PayrollContextSnapshot minimalRealSnapshot(CalculatePayrollUnitCommand command, List<PayrollConcept> concepts) {
            Map<String, Object> sourceKey = new LinkedHashMap<>();
            sourceKey.put("ruleSystemCode", command.ruleSystemCode());
            sourceKey.put("employeeTypeCode", command.employeeTypeCode());
            sourceKey.put("employeeNumber", command.employeeNumber());
            sourceKey.put("payrollPeriodCode", command.payrollPeriodCode());
            sourceKey.put("payrollTypeCode", command.payrollTypeCode());
            sourceKey.put("presenceNumber", command.presenceNumber());

            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("executionMode", PayrollExecutionMode.MINIMAL_REAL.name());
            payload.put("calculatedAt", LocalDateTime.now().toString());
            payload.put("conceptCount", concepts.size());
            payload.put("conceptCodes", concepts.stream().map(PayrollConcept::getConceptCode).toList());

            return new PayrollContextSnapshot(
                "EMPLOYEE_PAYROLL_CONTEXT",
                "PAYROLL_LAUNCH",
                toJson(sourceKey),
                toJson(payload)
            );
            }

            private String toJson(Map<String, Object> values) {
            StringBuilder out = new StringBuilder();
            out.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : values.entrySet()) {
                if (!first) {
                out.append(",");
                }
                first = false;
                out.append("\"").append(entry.getKey()).append("\":");
                Object value = entry.getValue();
                if (value == null) {
                out.append("null");
                } else if (value instanceof Number || value instanceof Boolean) {
                out.append(value);
                } else {
                out.append("\"").append(String.valueOf(value).replace("\"", "\\\"")).append("\"");
                }
            }
            out.append("}");
            return out.toString();
            }


    private List<PayrollConcept> fakeConcepts(CalculatePayrollUnitCommand command) {
        return List.of(
                new PayrollConcept(1, "BASE_FAKE", "Base fake amount", new BigDecimal("1000.00"), BigDecimal.ONE, new BigDecimal("1000.00"), "EARNING", command.payrollPeriodCode(), 1),
                new PayrollConcept(2, "SENIORITY_FAKE", "Seniority fake bonus", new BigDecimal("150.00"), BigDecimal.ONE, new BigDecimal("150.00"), "EARNING", command.payrollPeriodCode(), 2),
                new PayrollConcept(3, "TRANSPORT_FAKE", "Transport fake allowance", new BigDecimal("80.00"), BigDecimal.ONE, new BigDecimal("80.00"), "EARNING", command.payrollPeriodCode(), 3),
                new PayrollConcept(4, "BONUS_FAKE", "Performance fake bonus", new BigDecimal("120.00"), BigDecimal.ONE, new BigDecimal("120.00"), "EARNING", command.payrollPeriodCode(), 4),
                new PayrollConcept(5, "OVERTIME_FAKE", "Overtime fake amount", new BigDecimal("50.00"), BigDecimal.ONE, new BigDecimal("50.00"), "EARNING", command.payrollPeriodCode(), 5),
                new PayrollConcept(6, "GROSS_FAKE", "Gross fake amount", new BigDecimal("1400.00"), BigDecimal.ONE, new BigDecimal("1400.00"), "GROSS", command.payrollPeriodCode(), 6),
                new PayrollConcept(7, "TAX_FAKE", "Tax fake deduction", new BigDecimal("-210.00"), BigDecimal.ONE, new BigDecimal("210.00"), "DEDUCTION", command.payrollPeriodCode(), 7),
                new PayrollConcept(8, "SS_FAKE", "Social security fake deduction", new BigDecimal("-90.00"), BigDecimal.ONE, new BigDecimal("90.00"), "DEDUCTION", command.payrollPeriodCode(), 8),
                new PayrollConcept(9, "DEDUCTION_FAKE", "Other fake deduction", new BigDecimal("-20.00"), BigDecimal.ONE, new BigDecimal("20.00"), "DEDUCTION", command.payrollPeriodCode(), 9),
                new PayrollConcept(10, "NET_FAKE", "Net fake amount", new BigDecimal("1080.00"), BigDecimal.ONE, new BigDecimal("1080.00"), "NET", command.payrollPeriodCode(), 10)
        );
    }

    private List<PayrollWarning> fakeWarnings(CalculatePayrollUnitCommand command) {
        return List.of(new PayrollWarning(
                null,
                null,
                "DETERMINISTIC_FAKE_PAYROLL",
                "INFO",
                "Payroll generated by deterministic fake calculator",
                "{\"calculationEngineCode\":\"" + command.calculationEngineCode() + "\",\"calculationEngineVersion\":\""
                    + command.calculationEngineVersion() + "\",\"executionMode\":\"" + PayrollExecutionMode.FAKE.name() + "\",\"mode\":\"DETERMINISTIC_FAKE\"}"
        ));
    }

    private PayrollContextSnapshot fakeSnapshot(CalculatePayrollUnitCommand command) {
        return new PayrollContextSnapshot(
                "EMPLOYEE_PAYROLL_CONTEXT",
                "PAYROLL_LAUNCH",
                "{\"ruleSystemCode\":\"" + command.ruleSystemCode() + "\",\"employeeTypeCode\":\""
                        + command.employeeTypeCode() + "\",\"employeeNumber\":\"" + command.employeeNumber()
                        + "\",\"payrollPeriodCode\":\"" + command.payrollPeriodCode() + "\",\"payrollTypeCode\":\""
                        + command.payrollTypeCode() + "\",\"presenceNumber\":" + command.presenceNumber() + "}",
                "{\"calculationEngineCode\":\"" + command.calculationEngineCode() + "\",\"calculationEngineVersion\":\""
                    + command.calculationEngineVersion() + "\",\"executionMode\":\"" + PayrollExecutionMode.FAKE.name()
                    + "\",\"mode\":\"DETERMINISTIC_FAKE\"}"
        );
    }
}