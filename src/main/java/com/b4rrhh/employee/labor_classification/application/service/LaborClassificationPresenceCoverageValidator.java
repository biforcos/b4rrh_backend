package com.b4rrhh.employee.labor_classification.application.service;

import com.b4rrhh.employee.labor_classification.application.port.LaborClassificationPresenceConsistencyPort;
import com.b4rrhh.employee.labor_classification.application.port.PresencePeriod;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCoverageIncompleteException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOutsidePresencePeriodException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.temporal.support.DateRange;
import com.b4rrhh.employee.temporal.support.TimelineCoverageValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class LaborClassificationPresenceCoverageValidator {

    private final LaborClassificationPresenceConsistencyPort laborClassificationPresenceConsistencyPort;
    private final TimelineCoverageValidator timelineCoverageValidator = new TimelineCoverageValidator();

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
        DateRange candidatePeriod = new DateRange(startDate, endDate);
        List<DateRange> presenceRanges = toPresenceRanges(employeeId);

        if (!timelineCoverageValidator.isContained(List.of(candidatePeriod), presenceRanges)) {
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
        List<DateRange> presenceRanges = toPresenceRanges(employeeId);
        List<DateRange> laborClassificationRanges = projectedLaborClassificationHistory
                .stream()
                .map(laborClassification -> new DateRange(
                        laborClassification.getStartDate(),
                        laborClassification.getEndDate()
                ))
                .toList();

        for (DateRange laborClassificationRange : laborClassificationRanges) {
            if (!timelineCoverageValidator.isContained(List.of(laborClassificationRange), presenceRanges)) {
                throw new LaborClassificationOutsidePresencePeriodException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        laborClassificationRange.startDate(),
                        laborClassificationRange.endDate()
                );
            }
        }

        if (!timelineCoverageValidator.isFullyCovered(laborClassificationRanges, presenceRanges)) {
            throw new LaborClassificationCoverageIncompleteException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber
            );
        }
    }

    private List<DateRange> toPresenceRanges(Long employeeId) {
        return laborClassificationPresenceConsistencyPort
                .findPresencePeriodsByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .map(this::toDateRange)
                .toList();
    }

    private DateRange toDateRange(PresencePeriod presencePeriod) {
        return new DateRange(presencePeriod.startDate(), presencePeriod.endDate());
    }
}
