package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ReplaceLaborClassificationFromDateCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.application.service.AgreementCategoryRelationValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationCatalogValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationPresenceCoverageValidator;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationEmployeeNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOverlapException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.domain.port.LaborClassificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReplaceLaborClassificationFromDateService implements ReplaceLaborClassificationFromDateUseCase {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final LaborClassificationRepository laborClassificationRepository;
    private final EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort;
    private final LaborClassificationCatalogValidator laborClassificationCatalogValidator;
    private final AgreementCategoryRelationValidator agreementCategoryRelationValidator;
    private final LaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator;

    public ReplaceLaborClassificationFromDateService(
            LaborClassificationRepository laborClassificationRepository,
            EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort,
            LaborClassificationCatalogValidator laborClassificationCatalogValidator,
            AgreementCategoryRelationValidator agreementCategoryRelationValidator,
            LaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator
    ) {
        this.laborClassificationRepository = laborClassificationRepository;
        this.employeeLaborClassificationLookupPort = employeeLaborClassificationLookupPort;
        this.laborClassificationCatalogValidator = laborClassificationCatalogValidator;
        this.agreementCategoryRelationValidator = agreementCategoryRelationValidator;
        this.laborClassificationPresenceCoverageValidator = laborClassificationPresenceCoverageValidator;
    }

    @Override
    @Transactional
    public LaborClassification replaceFromDate(ReplaceLaborClassificationFromDateCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        LocalDate normalizedEffectiveDate = normalizeEffectiveDate(command.effectiveDate());

        EmployeeLaborClassificationContext employee = employeeLaborClassificationLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new LaborClassificationEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        String normalizedAgreementCode = laborClassificationCatalogValidator
                .normalizeRequiredCode("agreementCode", command.agreementCode());
        String normalizedAgreementCategoryCode = laborClassificationCatalogValidator
                .normalizeRequiredCode("agreementCategoryCode", command.agreementCategoryCode());

        laborClassificationCatalogValidator.validateAgreementCode(
                normalizedRuleSystemCode,
                normalizedAgreementCode,
                normalizedEffectiveDate
        );
        laborClassificationCatalogValidator.validateAgreementCategoryCode(
                normalizedRuleSystemCode,
                normalizedAgreementCategoryCode,
                normalizedEffectiveDate
        );
        agreementCategoryRelationValidator.validateAgreementCategoryRelation(
                normalizedRuleSystemCode,
                normalizedAgreementCode,
                normalizedAgreementCategoryCode,
                normalizedEffectiveDate
        );

        List<LaborClassification> currentHistory = laborClassificationRepository
                .findByEmployeeIdOrderByStartDate(employee.employeeId())
                .stream()
                .sorted(Comparator.comparing(LaborClassification::getStartDate))
                .toList();

        ReplacementPlan replacementPlan = buildReplacementPlan(
                employee.employeeId(),
                currentHistory,
                normalizedEffectiveDate,
                normalizedAgreementCode,
                normalizedAgreementCategoryCode
        );

        validateNoOverlap(
                replacementPlan.projectedHistory(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        laborClassificationPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                replacementPlan.projectedHistory(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        if (replacementPlan.periodToUpdate() != null) {
            laborClassificationRepository.update(replacementPlan.periodToUpdate());
        }
        if (replacementPlan.periodToSave() != null) {
            laborClassificationRepository.save(replacementPlan.periodToSave());
        }

        return replacementPlan.resultPeriod();
    }

    private ReplacementPlan buildReplacementPlan(
            Long employeeId,
            List<LaborClassification> currentHistory,
            LocalDate effectiveDate,
            String agreementCode,
            String agreementCategoryCode
    ) {
        LaborClassification coveringPeriod = currentHistory.stream()
                .filter(period -> isCoveringDate(period, effectiveDate))
                .findFirst()
                .orElse(null);

        if (coveringPeriod == null) {
            LaborClassification newPeriod = new LaborClassification(
                    employeeId,
                    agreementCode,
                    agreementCategoryCode,
                    effectiveDate,
                    null
            );

            List<LaborClassification> projected = new ArrayList<>(currentHistory);
            projected.add(newPeriod);
            projected.sort(Comparator.comparing(LaborClassification::getStartDate));

            return new ReplacementPlan(null, newPeriod, newPeriod, projected);
        }

        if (coveringPeriod.getStartDate().equals(effectiveDate)) {
            LaborClassification replaced = new LaborClassification(
                    employeeId,
                    agreementCode,
                    agreementCategoryCode,
                    coveringPeriod.getStartDate(),
                    coveringPeriod.getEndDate()
            );

            List<LaborClassification> projected = replaceByStartDate(currentHistory, replaced);
            return new ReplacementPlan(replaced, null, replaced, projected);
        }

        LaborClassification adjustedExisting = new LaborClassification(
                employeeId,
                coveringPeriod.getAgreementCode(),
                coveringPeriod.getAgreementCategoryCode(),
                coveringPeriod.getStartDate(),
                effectiveDate.minusDays(1)
        );

        LaborClassification replacement = new LaborClassification(
                employeeId,
                agreementCode,
                agreementCategoryCode,
                effectiveDate,
                coveringPeriod.getEndDate()
        );

        List<LaborClassification> projected = replaceByStartDate(currentHistory, adjustedExisting);
        projected.add(replacement);
        projected.sort(Comparator.comparing(LaborClassification::getStartDate));

        return new ReplacementPlan(adjustedExisting, replacement, replacement, projected);
    }

    private List<LaborClassification> replaceByStartDate(
            List<LaborClassification> history,
            LaborClassification updated
    ) {
        List<LaborClassification> projected = new ArrayList<>(history.size());
        for (LaborClassification laborClassification : history) {
            if (laborClassification.getStartDate().equals(updated.getStartDate())) {
                projected.add(updated);
            } else {
                projected.add(laborClassification);
            }
        }

        return projected;
    }

    private boolean isCoveringDate(LaborClassification period, LocalDate date) {
        if (period.getStartDate().isAfter(date)) {
            return false;
        }

        return period.getEndDate() == null || !period.getEndDate().isBefore(date);
    }

    private void validateNoOverlap(
            List<LaborClassification> projectedHistory,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<LaborClassification> sorted = projectedHistory.stream()
                .sorted(Comparator.comparing(LaborClassification::getStartDate))
                .toList();

        for (int index = 1; index < sorted.size(); index++) {
            LaborClassification previous = sorted.get(index - 1);
            LaborClassification current = sorted.get(index);
            LocalDate previousEnd = normalizeEndDate(previous.getEndDate());

            if (!current.getStartDate().isAfter(previousEnd)) {
                throw new LaborClassificationOverlapException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        current.getStartDate(),
                        current.getEndDate()
                );
            }
        }
    }

    private LocalDate normalizeEndDate(LocalDate endDate) {
        return endDate == null ? MAX_DATE : endDate;
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

    private LocalDate normalizeEffectiveDate(LocalDate effectiveDate) {
        if (effectiveDate == null) {
            throw new IllegalArgumentException("effectiveDate is required");
        }

        return effectiveDate;
    }

    private record ReplacementPlan(
            LaborClassification periodToUpdate,
            LaborClassification periodToSave,
            LaborClassification resultPeriod,
            List<LaborClassification> projectedHistory
    ) {
    }
}
