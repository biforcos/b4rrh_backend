package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateLaborClassificationService implements CreateLaborClassificationUseCase {

    private final LaborClassificationRepository laborClassificationRepository;
    private final EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort;
    private final LaborClassificationCatalogValidator laborClassificationCatalogValidator;
    private final AgreementCategoryRelationValidator agreementCategoryRelationValidator;
    private final LaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator;

    public CreateLaborClassificationService(
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
    public LaborClassification create(CreateLaborClassificationCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

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
                command.startDate()
        );
        laborClassificationCatalogValidator.validateAgreementCategoryCode(
                normalizedRuleSystemCode,
                normalizedAgreementCategoryCode,
                command.startDate()
        );
        agreementCategoryRelationValidator.validateAgreementCategoryRelation(
                normalizedRuleSystemCode,
                normalizedAgreementCode,
                normalizedAgreementCategoryCode,
                command.startDate()
        );

        LaborClassification newLaborClassification = new LaborClassification(
                employee.employeeId(),
                normalizedAgreementCode,
                normalizedAgreementCategoryCode,
                command.startDate(),
                command.endDate()
        );

        if (laborClassificationRepository.existsOverlappingPeriod(
                employee.employeeId(),
                newLaborClassification.getStartDate(),
                newLaborClassification.getEndDate(),
                null
        )) {
            throw new LaborClassificationOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    newLaborClassification.getStartDate(),
                    newLaborClassification.getEndDate()
            );
        }

        laborClassificationPresenceCoverageValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                newLaborClassification.getStartDate(),
                newLaborClassification.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<LaborClassification> projectedHistory = new ArrayList<>(
                laborClassificationRepository.findByEmployeeIdOrderByStartDate(employee.employeeId())
        );
        projectedHistory.add(newLaborClassification);

        laborClassificationPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        laborClassificationRepository.save(newLaborClassification);
        return newLaborClassification;
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
