package com.b4rrhh.employee.journey.application.usecase;

import com.b4rrhh.employee.journey.application.port.EmployeeJourneyLookupPort;
import com.b4rrhh.employee.journey.application.port.JourneyContractReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyContractRecord;
import com.b4rrhh.employee.journey.application.port.JourneyCostCenterReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyCostCenterRecord;
import com.b4rrhh.employee.journey.application.port.JourneyEmployeeContext;
import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationRecord;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceRecord;
import com.b4rrhh.employee.journey.application.port.JourneyWorkCenterReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyWorkCenterRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class GetEmployeeJourneyService implements GetEmployeeJourneyUseCase {

    private final EmployeeJourneyLookupPort employeeJourneyLookupPort;
    private final JourneyPresenceReadPort journeyPresenceReadPort;
    private final JourneyContractReadPort journeyContractReadPort;
    private final JourneyLaborClassificationReadPort journeyLaborClassificationReadPort;
    private final JourneyWorkCenterReadPort journeyWorkCenterReadPort;
    private final JourneyCostCenterReadPort journeyCostCenterReadPort;

    public GetEmployeeJourneyService(
            EmployeeJourneyLookupPort employeeJourneyLookupPort,
            JourneyPresenceReadPort journeyPresenceReadPort,
            JourneyContractReadPort journeyContractReadPort,
            JourneyLaborClassificationReadPort journeyLaborClassificationReadPort,
            JourneyWorkCenterReadPort journeyWorkCenterReadPort,
            JourneyCostCenterReadPort journeyCostCenterReadPort
    ) {
        this.employeeJourneyLookupPort = employeeJourneyLookupPort;
        this.journeyPresenceReadPort = journeyPresenceReadPort;
        this.journeyContractReadPort = journeyContractReadPort;
        this.journeyLaborClassificationReadPort = journeyLaborClassificationReadPort;
        this.journeyWorkCenterReadPort = journeyWorkCenterReadPort;
        this.journeyCostCenterReadPort = journeyCostCenterReadPort;
    }

    @Override
    public EmployeeJourneyView get(GetEmployeeJourneyCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        JourneyEmployeeContext employee = employeeJourneyLookupPort
                .findByBusinessKey(normalizedRuleSystemCode, normalizedEmployeeTypeCode, normalizedEmployeeNumber)
                .orElseThrow(() -> new JourneyEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        List<JourneyTrackView> tracks = List.of(
                buildPresenceTrack(employee.employeeId()),
                buildContractTrack(employee.employeeId()),
                buildLaborClassificationTrack(employee.employeeId()),
                buildWorkCenterTrack(employee.employeeId()),
                buildCostCenterTrack(employee.employeeId())
        );

        JourneyEmployeeHeaderView header = new JourneyEmployeeHeaderView(
                employee.ruleSystemCode(),
                employee.employeeTypeCode(),
                employee.employeeNumber(),
                employee.displayName()
        );

        return new EmployeeJourneyView(header, tracks);
    }

    private JourneyTrackView buildPresenceTrack(Long employeeId) {
        List<JourneyItemView> items = journeyPresenceReadPort.findByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .map(this::toPresenceItem)
                .toList();

        return new JourneyTrackView("PRESENCE", "Presence", items);
    }

    private JourneyTrackView buildContractTrack(Long employeeId) {
        List<JourneyItemView> items = journeyContractReadPort.findByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .map(this::toContractItem)
                .toList();

        return new JourneyTrackView("CONTRACT", "Contract", items);
    }

    private JourneyTrackView buildLaborClassificationTrack(Long employeeId) {
        List<JourneyItemView> items = journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .map(this::toLaborClassificationItem)
                .toList();

        return new JourneyTrackView("LABOR_CLASSIFICATION", "Labor classification", items);
    }

    private JourneyTrackView buildWorkCenterTrack(Long employeeId) {
        List<JourneyItemView> items = journeyWorkCenterReadPort.findByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .map(this::toWorkCenterItem)
                .toList();

        return new JourneyTrackView("WORK_CENTER", "Work center", items);
    }

    private JourneyTrackView buildCostCenterTrack(Long employeeId) {
        List<JourneyItemView> items = journeyCostCenterReadPort.findByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .map(this::toCostCenterItem)
                .toList();

        return new JourneyTrackView("COST_CENTER", "Cost center", items);
    }

    private JourneyItemView toPresenceItem(JourneyPresenceRecord presence) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("companyCode", presence.companyCode());
        details.put("entryReasonCode", presence.entryReasonCode());
        details.put("exitReasonCode", presence.exitReasonCode());

        return new JourneyItemView(
                presence.startDate(),
                presence.endDate(),
                presence.companyCode(),
                details
        );
    }

    private JourneyItemView toContractItem(JourneyContractRecord contract) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("contractCode", contract.contractCode());
        details.put("contractSubtypeCode", contract.contractSubtypeCode());

        return new JourneyItemView(
                contract.startDate(),
                contract.endDate(),
                contract.contractCode() + " / " + contract.contractSubtypeCode(),
                details
        );
    }

    private JourneyItemView toLaborClassificationItem(JourneyLaborClassificationRecord laborClassification) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("agreementCode", laborClassification.agreementCode());
        details.put("agreementCategoryCode", laborClassification.agreementCategoryCode());

        return new JourneyItemView(
                laborClassification.startDate(),
                laborClassification.endDate(),
                laborClassification.agreementCode() + " / " + laborClassification.agreementCategoryCode(),
                details
        );
    }

    private JourneyItemView toWorkCenterItem(JourneyWorkCenterRecord workCenter) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("workCenterCode", workCenter.workCenterCode());
        details.put("workCenterAssignmentNumber", workCenter.workCenterAssignmentNumber());
        details.put("startDate", workCenter.startDate());
        if (workCenter.endDate() != null) {
            details.put("endDate", workCenter.endDate());
        }

        return new JourneyItemView(
                workCenter.startDate(),
                workCenter.endDate(),
                workCenter.workCenterCode(),
                details
        );
    }

    private JourneyItemView toCostCenterItem(JourneyCostCenterRecord costCenter) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("costCenterCode", costCenter.costCenterCode());
        details.put("allocationPercentage", costCenter.allocationPercentage());

        return new JourneyItemView(
                costCenter.startDate(),
                costCenter.endDate(),
                costCenter.costCenterCode() + " (" + formatPercentage(costCenter.allocationPercentage()) + "%)",
                details
        );
    }

    private String formatPercentage(BigDecimal value) {
        if (value == null) {
            return "";
        }

        BigDecimal normalized = value.stripTrailingZeros();
        if (normalized.scale() < 0) {
            normalized = normalized.setScale(0);
        }

        return normalized.toPlainString();
    }

    private String normalizeRuleSystemCode(String ruleSystemCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }

        return ruleSystemCode.trim().toUpperCase();
    }

    private String normalizeEmployeeTypeCode(String employeeTypeCode) {
        if (employeeTypeCode == null || employeeTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeTypeCode is required");
        }

        return employeeTypeCode.trim().toUpperCase();
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }

        return employeeNumber.trim();
    }
}
