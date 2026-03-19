package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.application.service.LaborClassificationPresenceCoverageValidator;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAlreadyClosedException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationEmployeeNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.domain.port.LaborClassificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CloseLaborClassificationService implements CloseLaborClassificationUseCase {

    private final LaborClassificationRepository laborClassificationRepository;
    private final EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort;
    private final LaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator;

    public CloseLaborClassificationService(
            LaborClassificationRepository laborClassificationRepository,
            EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort,
            LaborClassificationPresenceCoverageValidator laborClassificationPresenceCoverageValidator
    ) {
        this.laborClassificationRepository = laborClassificationRepository;
        this.employeeLaborClassificationLookupPort = employeeLaborClassificationLookupPort;
        this.laborClassificationPresenceCoverageValidator = laborClassificationPresenceCoverageValidator;
    }

    @Override
    @Transactional
    public LaborClassification close(CloseLaborClassificationCommand command) {
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

        LaborClassification closed = existing.close(command.endDate());

        laborClassificationPresenceCoverageValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                closed.getStartDate(),
                closed.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<LaborClassification> projectedHistory = replaceByStartDate(
                laborClassificationRepository.findByEmployeeIdOrderByStartDate(employee.employeeId()),
                closed
        );

        laborClassificationPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        laborClassificationRepository.update(closed);
        return closed;
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
