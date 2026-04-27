package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.UpdateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.application.service.AgreementCategoryRelationValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationCatalogValidator;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationPresenceCoverageValidator;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAlreadyClosedException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationEmployeeNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOverlapException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.domain.port.LaborClassificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UpdateLaborClassificationService implements UpdateLaborClassificationUseCase {

    private final LaborClassificationRepository laborClassificationRepository;
    private final EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort;
    private final LaborClassificationCatalogValidator laborClassificationCatalogValidator;
    private final AgreementCategoryRelationValidator agreementCategoryRelationValidator;
    private final LaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator;

    public UpdateLaborClassificationService(
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
    public LaborClassification update(UpdateLaborClassificationCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        LocalDate normalizedStartDate = normalizeStartDate(command.startDate());

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

        LaborClassification existing = laborClassificationRepository
                .findByEmployeeIdAndStartDate(employee.employeeId(), normalizedStartDate)
                .orElseThrow(() -> new LaborClassificationNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedStartDate
                ));

        if (!existing.isActive()) {
            throw new LaborClassificationAlreadyClosedException(existing.getStartDate());
        }

        String normalizedAgreementCode = laborClassificationCatalogValidator
                .normalizeRequiredCode("agreementCode", command.agreementCode());
        String normalizedAgreementCategoryCode = laborClassificationCatalogValidator
                .normalizeRequiredCode("agreementCategoryCode", command.agreementCategoryCode());

        laborClassificationCatalogValidator.validateAgreementCode(
                normalizedRuleSystemCode,
                normalizedAgreementCode,
                existing.getStartDate()
        );
        laborClassificationCatalogValidator.validateAgreementCategoryCode(
                normalizedRuleSystemCode,
                normalizedAgreementCategoryCode,
                existing.getStartDate()
        );
        agreementCategoryRelationValidator.validateAgreementCategoryRelation(
                normalizedRuleSystemCode,
                normalizedAgreementCode,
                normalizedAgreementCategoryCode,
                existing.getStartDate()
        );

        LocalDate effectiveStartDate = (command.newStartDate() != null)
                ? command.newStartDate()
                : normalizedStartDate;

        LaborClassification updated = existing
                .correctStartDate(effectiveStartDate)
                .updateClassification(normalizedAgreementCode, normalizedAgreementCategoryCode);

        List<LaborClassification> fullHistory = laborClassificationRepository
                .findByEmployeeIdOrderByStartDate(employee.employeeId());

        LaborClassification cascadedPredecessor = null;
        if (!effectiveStartDate.equals(normalizedStartDate)) {
            LocalDate expectedPredecessorEnd = normalizedStartDate.minusDays(1);
            LaborClassification predecessor = fullHistory.stream()
                    .filter(c -> expectedPredecessorEnd.equals(c.getEndDate()))
                    .findFirst()
                    .orElse(null);
            if (predecessor != null) {
                cascadedPredecessor = predecessor.adjustEndDate(effectiveStartDate.minusDays(1));
                laborClassificationRepository.update(cascadedPredecessor, cascadedPredecessor.getStartDate());
            }
        }

        if (laborClassificationRepository.existsOverlappingPeriod(
                employee.employeeId(),
                updated.getStartDate(),
                updated.getEndDate(),
                normalizedStartDate
        )) {
            throw new LaborClassificationOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    updated.getStartDate(),
                    updated.getEndDate()
            );
        }

        laborClassificationPresenceCoverageValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                updated.getStartDate(),
                updated.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<LaborClassification> projectedHistory = buildProjectedHistory(
                fullHistory,
                cascadedPredecessor,
                updated,
                normalizedStartDate
        );

        laborClassificationPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        laborClassificationRepository.update(updated, normalizedStartDate);
        return updated;
    }

    private List<LaborClassification> buildProjectedHistory(
            List<LaborClassification> history,
            LaborClassification cascadedPredecessor,
            LaborClassification updated,
            LocalDate oldStartDate
    ) {
        List<LaborClassification> projected = new ArrayList<>(history.size());
        for (LaborClassification lc : history) {
            if (lc.getStartDate().equals(oldStartDate)) {
                projected.add(updated);
            } else if (cascadedPredecessor != null
                    && lc.getStartDate().equals(cascadedPredecessor.getStartDate())) {
                projected.add(cascadedPredecessor);
            } else {
                projected.add(lc);
            }
        }
        return projected;
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

    private LocalDate normalizeStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }

        return startDate;
    }
}
