package com.b4rrhh.employee.workcenter.application.service;

import com.b4rrhh.employee.workcenter.application.port.PresencePeriod;
import com.b4rrhh.employee.workcenter.application.port.WorkCenterPresenceConsistencyPort;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterPresenceCoverageGapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class DefaultWorkCenterPresenceConsistencyValidator implements WorkCenterPresenceConsistencyValidator {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final WorkCenterPresenceConsistencyPort workCenterPresenceConsistencyPort;
    private final boolean requireFullPresenceCoverage;

    public DefaultWorkCenterPresenceConsistencyValidator(
            WorkCenterPresenceConsistencyPort workCenterPresenceConsistencyPort,
            @Value("${employee.work-center.require-full-presence-coverage:false}")
            boolean requireFullPresenceCoverage
    ) {
        this.workCenterPresenceConsistencyPort = workCenterPresenceConsistencyPort;
        this.requireFullPresenceCoverage = requireFullPresenceCoverage;
    }

    @Override
    public void validatePeriodWithinPresence(
            Long employeeId,
            LocalDate startDate,
            LocalDate endDate,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        if (!workCenterPresenceConsistencyPort.existsPresenceContainingPeriod(employeeId, startDate, endDate)) {
            throw new WorkCenterOutsidePresencePeriodException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    startDate,
                    endDate
            );
        }
    }

    @Override
    public void validatePresenceCoverageIfRequired(
            Long employeeId,
            List<WorkCenter> projectedWorkCenterHistory,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        if (!requireFullPresenceCoverage) {
            return;
        }

        List<PresencePeriod> presencePeriods = workCenterPresenceConsistencyPort
                .findPresencePeriodsByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .sorted(Comparator.comparing(PresencePeriod::startDate))
                .toList();

        List<WorkCenter> sortedWorkCenters = projectedWorkCenterHistory
                .stream()
                .sorted(Comparator.comparing(WorkCenter::getStartDate))
                .toList();

        if (!isFullyCovered(presencePeriods, sortedWorkCenters)) {
            throw new WorkCenterPresenceCoverageGapException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber
            );
        }
    }

    private boolean isFullyCovered(List<PresencePeriod> presencePeriods, List<WorkCenter> workCenters) {
        if (presencePeriods.isEmpty()) {
            return workCenters.isEmpty();
        }

        for (PresencePeriod presencePeriod : presencePeriods) {
            LocalDate presenceStart = presencePeriod.startDate();
            LocalDate presenceEnd = normalizeEndDate(presencePeriod.endDate());
            LocalDate cursor = presenceStart;

            for (WorkCenter workCenter : workCenters) {
                LocalDate workCenterStart = workCenter.getStartDate();
                LocalDate workCenterEnd = normalizeEndDate(workCenter.getEndDate());

                if (workCenterEnd.isBefore(presenceStart) || workCenterStart.isAfter(presenceEnd)) {
                    continue;
                }

                LocalDate effectiveStart = workCenterStart.isBefore(presenceStart) ? presenceStart : workCenterStart;
                LocalDate effectiveEnd = workCenterEnd.isAfter(presenceEnd) ? presenceEnd : workCenterEnd;

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