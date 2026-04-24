package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputLookupPort;
import com.b4rrhh.payroll.application.service.PayrollConceptExecutionContext;
import com.b4rrhh.payroll.application.service.PayrollConceptExecutionResult;
import com.b4rrhh.payroll.application.service.PayrollConceptGraphCalculator;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollConcept;
import com.b4rrhh.payroll.domain.model.PayrollContextSnapshot;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import com.b4rrhh.payroll.domain.model.PayrollWarning;
import com.b4rrhh.payroll.infrastructure.config.PayrollLaunchExecutionProperties;
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
    private final PayrollLaunchEligibleInputLookupPort payrollLaunchEligibleInputLookupPort;
    private final PayrollLaunchExecutionProperties payrollLaunchExecutionProperties;
    private final PayrollConceptGraphCalculator payrollConceptGraphCalculator;

    public CalculatePayrollUnitService(
            CalculatePayrollUseCase calculatePayrollUseCase,
            PayrollLaunchEligibleInputLookupPort payrollLaunchEligibleInputLookupPort,
            PayrollLaunchExecutionProperties payrollLaunchExecutionProperties,
            PayrollConceptGraphCalculator payrollConceptGraphCalculator
    ) {
        this.calculatePayrollUseCase = calculatePayrollUseCase;
        this.payrollLaunchEligibleInputLookupPort = payrollLaunchEligibleInputLookupPort;
        this.payrollLaunchExecutionProperties = payrollLaunchExecutionProperties;
        this.payrollConceptGraphCalculator = payrollConceptGraphCalculator;
    }

    @Override
    public Payroll calculate(CalculatePayrollUnitCommand command) {
        return switch (payrollLaunchExecutionProperties.getMode()) {
            case ELIGIBLE_REAL -> calculateEligibleReal(command);
            case MINIMAL_REAL -> throw new UnsupportedOperationException(
                    "MINIMAL_REAL is no longer supported in CalculatePayrollUnitService. Use ELIGIBLE_REAL."
            );
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
            if (input.agreementCode() == null || input.agreementCode().isBlank()) {
                throw new PayrollLaunchInputMissingException(
                    "AGREEMENT_CODE_MISSING",
                    "Eligible real execution skipped: agreementCode is required but missing in launcher context",
                    Map.of("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name())
                );
            }
            if (input.agreementCategoryCode() == null || input.agreementCategoryCode().isBlank()) {
                throw new PayrollLaunchInputMissingException(
                    "AGREEMENT_CATEGORY_MISSING",
                    "Eligible real execution skipped: agreementCategoryCode is required but missing in launcher context",
                    Map.of("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name())
                );
            }

                PayrollConceptExecutionContext context = new PayrollConceptExecutionContext(
                    command.ruleSystemCode(),
                    input.agreementCode(),
                    input.agreementCategoryCode(),
                    command.periodEnd()
            );

            PayrollConceptExecutionResult conceptResult = payrollConceptGraphCalculator.calculateConceptResult("101", context);
            PayrollConcept payrollConcept = new PayrollConcept(
                    1,
                    "101",
                    "SALARIO_BASE",
                    conceptResult.amount(),
                    conceptResult.quantity(),
                    conceptResult.rate(),
                    "EARNING",
                    command.payrollPeriodCode(),
                    1
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
                List.of(eligibleRealWarning(command, input, payrollConcept)),
                List.of(payrollConcept),
                List.of(eligibleRealSnapshot(command, input, payrollConcept))
            ));
            }

            private PayrollWarning eligibleRealWarning(
                CalculatePayrollUnitCommand command,
                PayrollLaunchEligibleInputContext input,
                PayrollConcept concept
            ) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name());
            details.put("employeeTypeCode", command.employeeTypeCode());
            details.put("employeeNumber", command.employeeNumber());
            details.put("agreementCode", input.agreementCode());
            details.put("agreementCategoryCode", input.agreementCategoryCode());
            details.put("conceptCode", concept.getConceptCode());
            details.put("amount", concept.getAmount());
            details.put("quantity", concept.getQuantity());
            details.put("rate", concept.getRate());
            return new PayrollWarning(
                null,
                null,
                "ELIGIBLE_REAL_EXECUTION",
                "INFO",
                "Payroll generated by eligible real minimal concept execution",
                toJson(details)
            );
            }

            private PayrollContextSnapshot eligibleRealSnapshot(
                CalculatePayrollUnitCommand command,
                PayrollLaunchEligibleInputContext input,
                PayrollConcept concept
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
            payload.put("agreementCode", input.agreementCode());
            payload.put("agreementCategoryCode", input.agreementCategoryCode());
            payload.put("conceptCode", concept.getConceptCode());
            payload.put("amount", concept.getAmount());
            payload.put("quantity", concept.getQuantity());
            payload.put("rate", concept.getRate());

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