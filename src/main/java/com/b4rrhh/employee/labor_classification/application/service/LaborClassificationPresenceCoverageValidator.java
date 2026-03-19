package com.b4rrhh.employee.labor_classification.application.service;

import com.b4rrhh.employee.labor_classification.application.port.LaborClassificationPresenceConsistencyPort;
import com.b4rrhh.employee.labor_classification.application.port.PresencePeriod;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCoverageIncompleteException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOutsidePresencePeriodException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class LaborClassificationPresenceCoverageValidator {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final LaborClassificationPresenceConsistencyPort laborClassificationPresenceConsistencyPort;

    public LaborClassificationPresenceCoverageValidator(
            LaborClassificationPresenceConsistencyPort laborClassificationPresenceConsistencyPort
    ) {
        this.laborClassificationPresenceConsistencyPort = laborClassificationPresenceConsistencyPort;
    }

    public void validatePeriodWithinPresence(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        if (!laborClassificationPresenceConsistencyPort.existsPresenceContainingPeriod(employeeId, startDate, endDate)) {
            throw new LaborClassificationOutsidePresencePeriodException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    startDate,
                    endDate
            );
        }
    }

    public void validateFullCoverage(
            Long employeeId,
            List<LaborClassification> projectedLaborClassificationHistory,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<PresencePeriod> presencePeriods = laborClassificationPresenceConsistencyPort
                .findPresencePeriodsByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .sorted(Comparator.comparing(PresencePeriod::startDate))
                .toList();

        List<LaborClassification> sortedClassifications = projectedLaborClassificationHistory
                .stream()
                .sorted(Comparator.comparing(LaborClassification::getStartDate))
                .toList();

        for (LaborClassification laborClassification : sortedClassifications) {
            validatePeriodWithinPresence(
                    employeeId,
                    laborClassification.getStartDate(),
                    laborClassification.getEndDate(),
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber
            );
        }

        if (!isFullyCovered(presencePeriods, sortedClassifications)) {
            throw new LaborClassificationCoverageIncompleteException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber
            );
        }
    }

    private boolean isFullyCovered(
            List<PresencePeriod> presencePeriods,
            List<LaborClassification> laborClassifications
    ) {
        if (presencePeriods.isEmpty()) {
            return laborClassifications.isEmpty();
        }

        for (PresencePeriod presencePeriod : presencePeriods) {
            LocalDate presenceStart = presencePeriod.startDate();
            LocalDate presenceEnd = normalizeEndDate(presencePeriod.endDate());
            LocalDate cursor = presenceStart;

            for (LaborClassification laborClassification : laborClassifications) {
                LocalDate classificationStart = laborClassification.getStartDate();
                LocalDate classificationEnd = normalizeEndDate(laborClassification.getEndDate());

                if (classificationEnd.isBefore(presenceStart) || classificationStart.isAfter(presenceEnd)) {
                    continue;
                }

                LocalDate effectiveStart = classificationStart.isBefore(presenceStart)
                        ? presenceStart
                        : classificationStart;
                LocalDate effectiveEnd = classificationEnd.isAfter(presenceEnd)
                        ? presenceEnd
                        : classificationEnd;

                if (effectiveStart.isAfter(cursor)) {
                    return false;
                }

                if (!effectiveEnd.isBefore(cursor)) {
                    cursor = advanceCursor(effectiveEnd);
                }

                if (isCursorBeyondPresenceEnd(cursor, presenceEnd)) {
                    break;
                }
            }

            if (!isCursorBeyondPresenceEnd(cursor, presenceEnd)) {
                return false;
            }
        }

        return true;
    }

    private boolean isCursorBeyondPresenceEnd(LocalDate cursor, LocalDate presenceEnd) {
        return cursor.isAfter(presenceEnd) || (MAX_DATE.equals(presenceEnd) && MAX_DATE.equals(cursor));
    }

    private LocalDate normalizeEndDate(LocalDate endDate) {
        return endDate == null ? MAX_DATE : endDate;
    }

    private LocalDate advanceCursor(LocalDate endDate) {
        if (MAX_DATE.equals(endDate)) {
            return MAX_DATE;
        }

        return endDate.plusDays(1);
    }
}
