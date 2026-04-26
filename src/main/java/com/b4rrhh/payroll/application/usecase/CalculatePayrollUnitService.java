package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.application.port.CompanyProfileContext;
import com.b4rrhh.payroll.application.port.CompanyProfileLookupPort;
import com.b4rrhh.payroll.application.port.EmployeePersonalDataContext;
import com.b4rrhh.payroll.application.port.EmployeePersonalDataLookupPort;
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
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;
import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.execution.domain.model.AggregateSourceEntry;
import com.b4rrhh.payroll_engine.execution.domain.model.ConceptExecutionPlanEntry;
import com.b4rrhh.payroll_engine.planning.application.service.BuildEligibleExecutionPlanUseCase;
import com.b4rrhh.payroll_engine.planning.domain.model.EligibleExecutionPlanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CalculatePayrollUnitService implements CalculatePayrollUnitUseCase {

    private static final Logger log = LoggerFactory.getLogger(CalculatePayrollUnitService.class);

    private final CalculatePayrollUseCase calculatePayrollUseCase;
    private final PayrollLaunchEligibleInputLookupPort payrollLaunchEligibleInputLookupPort;
    private final PayrollLaunchExecutionProperties payrollLaunchExecutionProperties;
    private final PayrollConceptGraphCalculator payrollConceptGraphCalculator;
    private final BuildEligibleExecutionPlanUseCase buildEligibleExecutionPlanUseCase;
    private final CompanyProfileLookupPort companyProfileLookupPort;
    private final EmployeePersonalDataLookupPort employeePersonalDataLookupPort;

    public CalculatePayrollUnitService(
            CalculatePayrollUseCase calculatePayrollUseCase,
            PayrollLaunchEligibleInputLookupPort payrollLaunchEligibleInputLookupPort,
            PayrollLaunchExecutionProperties payrollLaunchExecutionProperties,
            PayrollConceptGraphCalculator payrollConceptGraphCalculator,
            BuildEligibleExecutionPlanUseCase buildEligibleExecutionPlanUseCase,
            CompanyProfileLookupPort companyProfileLookupPort,
            EmployeePersonalDataLookupPort employeePersonalDataLookupPort
    ) {
        this.calculatePayrollUseCase = calculatePayrollUseCase;
        this.payrollLaunchEligibleInputLookupPort = payrollLaunchEligibleInputLookupPort;
        this.payrollLaunchExecutionProperties = payrollLaunchExecutionProperties;
        this.payrollConceptGraphCalculator = payrollConceptGraphCalculator;
        this.buildEligibleExecutionPlanUseCase = buildEligibleExecutionPlanUseCase;
        this.companyProfileLookupPort = companyProfileLookupPort;
        this.employeePersonalDataLookupPort = employeePersonalDataLookupPort;
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

    private Payroll calculateEligibleReal(CalculatePayrollUnitCommand command) {
        log.info("[NÓMINA] ▶ Iniciando cálculo ELIGIBLE_REAL | empleado={} tipo={} periodo={} presencia={}",
                command.employeeNumber(), command.employeeTypeCode(),
                command.payrollPeriodCode(), command.presenceNumber());

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

        log.info("[NÓMINA] Contexto resuelto | empresa={} convenio={} categoría={} ventanas={}",
                input.companyCode(), input.agreementCode(), input.agreementCategoryCode(),
                input.workingTimeWindows() != null ? input.workingTimeWindows().size() : 0);

        EmployeeAssignmentContext assignmentContext = new EmployeeAssignmentContext(
                command.ruleSystemCode(),
                input.companyCode(),
                input.agreementCode(),
                command.employeeTypeCode()
        );

        log.debug("[ENGINE] Construyendo plan de ejecución | RS={} convenio={} ref={}",
                command.ruleSystemCode(), input.agreementCode(), command.periodEnd());
        EligibleExecutionPlanResult planResult =
                buildEligibleExecutionPlanUseCase.build(assignmentContext, command.periodEnd());

        List<ConceptExecutionPlanEntry> plan = planResult.executionPlan();
        log.info("[NÓMINA] Plan de ejecución: {} pasos → {}",
                plan.size(),
                plan.stream().map(e -> e.identity().getConceptCode()).collect(Collectors.joining(" → ")));

        Map<String, com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept> engineConceptByCode =
                planResult.expandedConcepts().stream()
                        .collect(Collectors.toMap(
                                c -> c.getConceptCode(),
                                c -> c
                        ));

        PayrollConceptExecutionContext calcContext = new PayrollConceptExecutionContext(
                command.ruleSystemCode(),
                input.agreementCode(),
                input.agreementCategoryCode(),
                command.periodEnd()
        );

        Map<ConceptNodeIdentity, BigDecimal> executionState = new HashMap<>();
        Map<String, BigDecimal> amountByCode = new LinkedHashMap<>();
        Map<String, BigDecimal> quantityByCode = new LinkedHashMap<>();
        Map<String, BigDecimal> rateByCode = new LinkedHashMap<>();

        int step = 0;
        int total = plan.size();
        for (ConceptExecutionPlanEntry entry : plan) {
            step++;
            String conceptCode = entry.identity().getConceptCode();
            BigDecimal amount;
            BigDecimal quantity = null;
            BigDecimal rate = null;

            switch (entry.calculationType()) {
                case DIRECT_AMOUNT -> {
                    log.info("[NÓMINA] [{}/{}] {} DIRECT_AMOUNT → calculador externo",
                            step, total, conceptCode);
                    PayrollConceptExecutionResult result =
                            payrollConceptGraphCalculator.calculateConceptResult(conceptCode, calcContext);
                    amount = result.amount();
                    quantity = result.quantity();
                    rate = result.rate();
                    log.info("[NÓMINA] [{}/{}] {} = {} (cant={} tasa={})",
                            step, total, conceptCode, amount, quantity, rate);
                }
                case RATE_BY_QUANTITY -> {
                    ConceptNodeIdentity quantityId = entry.operands().get(OperandRole.QUANTITY);
                    ConceptNodeIdentity rateId = entry.operands().get(OperandRole.RATE);
                    quantity = requireStateAmount(executionState, quantityId);
                    rate = requireStateAmount(executionState, rateId);
                    amount = quantity.multiply(rate).setScale(2, RoundingMode.HALF_UP);
                    log.info("[NÓMINA] [{}/{}] {} RATE_BY_QUANTITY → {}({}) × {}({}) = {}",
                            step, total, conceptCode,
                            quantityId.getConceptCode(), quantity,
                            rateId.getConceptCode(), rate,
                            amount);
                }
                case PERCENTAGE -> {
                    ConceptNodeIdentity baseId = entry.operands().get(OperandRole.BASE);
                    ConceptNodeIdentity pctId = entry.operands().get(OperandRole.PERCENTAGE);
                    BigDecimal base = requireStateAmount(executionState, baseId);
                    BigDecimal pct = requireStateAmount(executionState, pctId);
                    amount = base.multiply(pct).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    log.info("[NÓMINA] [{}/{}] {} PERCENTAGE → {}({}) × {}% = {}",
                            step, total, conceptCode,
                            baseId.getConceptCode(), base, pct, amount);
                }
                case AGGREGATE -> {
                    BigDecimal sum = BigDecimal.ZERO;
                    StringBuilder sourceDesc = new StringBuilder();
                    for (AggregateSourceEntry source : entry.aggregateSources()) {
                        BigDecimal sourceAmount = requireStateAmount(executionState, source.identity());
                        sum = sum.add(source.invertSign() ? sourceAmount.negate() : sourceAmount);
                        if (!sourceDesc.isEmpty()) sourceDesc.append(" + ");
                        if (source.invertSign()) {
                            sourceDesc.append("-").append(source.identity().getConceptCode())
                                    .append("(").append(sourceAmount).append(")");
                        } else {
                            sourceDesc.append(source.identity().getConceptCode())
                                    .append("(").append(sourceAmount).append(")");
                        }
                    }
                    amount = sum.setScale(2, RoundingMode.HALF_UP);
                    log.info("[NÓMINA] [{}/{}] {} AGGREGATE → {} = {}",
                            step, total, conceptCode, sourceDesc, amount);
                }
                default -> throw new UnsupportedOperationException(
                        "Unsupported calculation type: " + entry.calculationType()
                );
            }

            executionState.put(entry.identity(), amount);
            amountByCode.put(conceptCode, amount);
            quantityByCode.put(conceptCode, quantity);
            rateByCode.put(conceptCode, rate);
        }

        List<ConceptRow> rows = new ArrayList<>();
        for (String conceptCode : amountByCode.keySet()) {
            com.b4rrhh.payroll_engine.concept.domain.model.PayrollConcept engineConcept =
                    engineConceptByCode.get(conceptCode);
            if (engineConcept.getPayslipOrderCode() == null) {
                continue;
            }
            int displayOrder = Integer.parseInt(engineConcept.getPayslipOrderCode());
            rows.add(new ConceptRow(
                    conceptCode,
                    engineConcept.getConceptMnemonic(),
                    amountByCode.get(conceptCode),
                    quantityByCode.get(conceptCode),
                    rateByCode.get(conceptCode),
                    engineConcept.getFunctionalNature().name(),
                    displayOrder
            ));
        }
        rows.sort(Comparator.comparingInt(ConceptRow::displayOrder));

        log.info("[NÓMINA] Filtro recibo | {} de {} conceptos con payslipOrderCode → [{}]",
                rows.size(), amountByCode.size(),
                rows.stream().map(ConceptRow::conceptCode).collect(Collectors.joining(", ")));

        List<PayrollConcept> payrollConcepts = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ConceptRow r = rows.get(i);
            payrollConcepts.add(new PayrollConcept(
                    i + 1,
                    r.conceptCode(),
                    r.mnemonic(),
                    r.amount(),
                    r.quantity(),
                    r.rate(),
                    r.nature(),
                    command.payrollPeriodCode(),
                    r.displayOrder()
            ));
        }

        Payroll result = calculatePayrollUseCase.calculate(new CalculatePayrollCommand(
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
                List.of(eligibleRealWarning(command, input)),
                payrollConcepts,
                buildSnapshots(command, input)
        ));
        log.info("[NÓMINA] ✓ Cálculo completado | empleado={} periodo={} → {} líneas en recibo",
                command.employeeNumber(), command.payrollPeriodCode(), payrollConcepts.size());
        return result;
    }

    private record ConceptRow(
            String conceptCode,
            String mnemonic,
            BigDecimal amount,
            BigDecimal quantity,
            BigDecimal rate,
            String nature,
            int displayOrder
    ) {}

    private BigDecimal requireStateAmount(Map<ConceptNodeIdentity, BigDecimal> state, ConceptNodeIdentity id) {
        BigDecimal value = state.get(id);
        if (value == null) {
            throw new IllegalStateException(
                    "Required concept result not yet computed for: " + id +
                    ". Check that the execution plan is in topological order."
            );
        }
        return value;
    }

    private PayrollWarning eligibleRealWarning(
            CalculatePayrollUnitCommand command,
            PayrollLaunchEligibleInputContext input
    ) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("executionMode", PayrollExecutionMode.ELIGIBLE_REAL.name());
        details.put("employeeTypeCode", command.employeeTypeCode());
        details.put("employeeNumber", command.employeeNumber());
        details.put("agreementCode", input.agreementCode());
        details.put("agreementCategoryCode", input.agreementCategoryCode());
        return new PayrollWarning(
                null,
                null,
                "ELIGIBLE_REAL_EXECUTION",
                "INFO",
                "Payroll generated by eligible real minimal concept execution",
                toJson(details)
        );
    }

    private List<PayrollContextSnapshot> buildSnapshots(
            CalculatePayrollUnitCommand command,
            PayrollLaunchEligibleInputContext input
    ) {
        List<PayrollContextSnapshot> snapshots = new ArrayList<>();
        snapshots.add(eligibleRealSnapshot(command, input));

        if (input.companyCode() != null) {
            companyProfileLookupPort
                    .findByRuleSystemAndCode(command.ruleSystemCode(), input.companyCode())
                    .map(cp -> buildCompanySnapshot(command, input.companyCode(), cp))
                    .ifPresent(snapshots::add);
        }

        employeePersonalDataLookupPort
                .findByBusinessKey(command.ruleSystemCode(), command.employeeTypeCode(),
                        command.employeeNumber(), command.periodEnd())
                .map(ep -> buildEmployeeSnapshot(command, ep))
                .ifPresent(snapshots::add);

        return List.copyOf(snapshots);
    }

    private PayrollContextSnapshot buildCompanySnapshot(
            CalculatePayrollUnitCommand command,
            String companyCode,
            CompanyProfileContext cp
    ) {
        Map<String, Object> sourceKey = new LinkedHashMap<>();
        sourceKey.put("ruleSystemCode", command.ruleSystemCode());
        sourceKey.put("entityTypeCode", "COMPANY");
        sourceKey.put("entityCode", companyCode);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("legalName", cp.legalName());
        payload.put("taxIdentifier", cp.taxIdentifier());
        payload.put("street", cp.street());
        payload.put("city", cp.city());
        payload.put("postalCode", cp.postalCode());

        return new PayrollContextSnapshot("COMPANY_DATA", "RULESYSTEM", toJson(sourceKey), toJson(payload));
    }

    private PayrollContextSnapshot buildEmployeeSnapshot(
            CalculatePayrollUnitCommand command,
            EmployeePersonalDataContext ep
    ) {
        Map<String, Object> sourceKey = new LinkedHashMap<>();
        sourceKey.put("ruleSystemCode", command.ruleSystemCode());
        sourceKey.put("employeeTypeCode", command.employeeTypeCode());
        sourceKey.put("employeeNumber", command.employeeNumber());

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("fullName", ep.fullName());
        payload.put("nif", ep.nif());
        payload.put("street", ep.street());
        payload.put("city", ep.city());
        payload.put("postalCode", ep.postalCode());

        return new PayrollContextSnapshot("EMPLOYEE_DATA", "EMPLOYEE", toJson(sourceKey), toJson(payload));
    }

    private PayrollContextSnapshot eligibleRealSnapshot(
            CalculatePayrollUnitCommand command,
            PayrollLaunchEligibleInputContext input
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

        return new PayrollContextSnapshot(
                "EMPLOYEE_PAYROLL_CONTEXT",
                "PAYROLL_LAUNCH",
                toJson(sourceKey),
                toJson(payload)
        );
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
}
