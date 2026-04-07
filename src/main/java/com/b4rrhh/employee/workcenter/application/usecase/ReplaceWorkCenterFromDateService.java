package com.b4rrhh.employee.workcenter.application.usecase;

import com.b4rrhh.employee.temporal.support.DateRange;
import com.b4rrhh.employee.temporal.support.ReplaceMode;
import com.b4rrhh.employee.temporal.support.StrongTimelineReplacePlan;
import com.b4rrhh.employee.temporal.support.StrongTimelineReplacePlanner;
import com.b4rrhh.employee.temporal.support.TemporalDates;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterContext;
import com.b4rrhh.employee.workcenter.application.port.EmployeeWorkCenterLookupPort;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterCatalogValidator;
import com.b4rrhh.employee.workcenter.application.service.WorkCenterPresenceConsistencyValidator;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterEmployeeNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterRuleSystemNotFoundException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterRepository;
import com.b4rrhh.employee.workcenter.domain.service.WorkCenterEmployeeCompanyDomainService;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReplaceWorkCenterFromDateService implements ReplaceWorkCenterFromDateUseCase {

    private static final StrongTimelineReplacePlanner STRONG_TIMELINE_REPLACE_PLANNER =
            new StrongTimelineReplacePlanner();

    private final WorkCenterRepository workCenterRepository;
    private final EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final WorkCenterCatalogValidator workCenterCatalogValidator;
    private final WorkCenterPresenceConsistencyValidator workCenterPresenceConsistencyValidator;
    private final WorkCenterEmployeeCompanyDomainService workCenterEmployeeCompanyDomainService;

    public ReplaceWorkCenterFromDateService(
            WorkCenterRepository workCenterRepository,
            EmployeeWorkCenterLookupPort employeeWorkCenterLookupPort,
            RuleSystemRepository ruleSystemRepository,
            WorkCenterCatalogValidator workCenterCatalogValidator,
            WorkCenterPresenceConsistencyValidator workCenterPresenceConsistencyValidator,
                WorkCenterEmployeeCompanyDomainService workCenterEmployeeCompanyDomainService
    ) {
        this.workCenterRepository = workCenterRepository;
        this.employeeWorkCenterLookupPort = employeeWorkCenterLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
        this.workCenterCatalogValidator = workCenterCatalogValidator;
        this.workCenterPresenceConsistencyValidator = workCenterPresenceConsistencyValidator;
        this.workCenterEmployeeCompanyDomainService = workCenterEmployeeCompanyDomainService;
    }

    @Override
    @Transactional
    public WorkCenter replaceFromDate(ReplaceWorkCenterFromDateCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        LocalDate normalizedEffectiveDate = normalizeEffectiveDate(command.effectiveDate());

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new WorkCenterRuleSystemNotFoundException(normalizedRuleSystemCode));

        EmployeeWorkCenterContext employee = employeeWorkCenterLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new WorkCenterEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        String normalizedWorkCenterCode = workCenterCatalogValidator
                .normalizeRequiredCode("workCenterCode", command.workCenterCode());
        workCenterCatalogValidator.validateWorkCenterCode(
                normalizedRuleSystemCode,
                normalizedWorkCenterCode,
                normalizedEffectiveDate
        );

        List<WorkCenter> currentHistory = workCenterRepository
                .findByEmployeeIdOrderByStartDate(employee.employeeId())
                .stream()
                .sorted(Comparator.comparing(WorkCenter::getStartDate))
                .toList();

        ReplacementPlan replacementPlan = buildReplacementPlan(
                employee.employeeId(),
                currentHistory,
                normalizedEffectiveDate,
                normalizedWorkCenterCode,
                nextAssignmentNumber(employee.employeeId())
        );

        workCenterPresenceConsistencyValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                replacementPlan.resultPeriod().getStartDate(),
                replacementPlan.resultPeriod().getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        workCenterEmployeeCompanyDomainService.validateWorkCenterBelongsToEmployeeCompany(
                employee.employeeId(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber,
                normalizedWorkCenterCode,
                normalizedEffectiveDate
        );

        validateNoOverlap(
                replacementPlan.projectedHistory(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        workCenterPresenceConsistencyValidator.validatePresenceCoverageIfRequired(
                employee.employeeId(),
                replacementPlan.projectedHistory(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        if (replacementPlan.periodToUpdate() != null) {
            workCenterRepository.save(replacementPlan.periodToUpdate());
        }
        if (replacementPlan.periodToSave() != null) {
            workCenterRepository.save(replacementPlan.periodToSave());
        }

        return replacementPlan.resultPeriod();
    }

    private ReplacementPlan buildReplacementPlan(
            Long employeeId,
            List<WorkCenter> currentHistory,
            LocalDate effectiveDate,
            String workCenterCode,
            int nextAssignmentNumber
    ) {
        StrongTimelineReplacePlan timelinePlan = STRONG_TIMELINE_REPLACE_PLANNER.plan(
                toDateRanges(currentHistory),
                effectiveDate
        );

        if (timelinePlan.mode() == ReplaceMode.NO_COVERING) {
            DateRange insertRange = timelinePlan.periodToInsert();
            WorkCenter replacement = new WorkCenter(
                    null,
                    employeeId,
                    nextAssignmentNumber,
                    workCenterCode,
                    insertRange.startDate(),
                    insertRange.endDate(),
                    null,
                    null
            );

            List<WorkCenter> projected = new ArrayList<>(currentHistory);
            projected.add(replacement);
            projected.sort(Comparator.comparing(WorkCenter::getStartDate));

            return new ReplacementPlan(null, replacement, replacement, projected);
        }

        WorkCenter coveringPeriod = getPeriodByIndex(currentHistory, timelinePlan.coveringPeriodIndex());

        if (timelinePlan.mode() == ReplaceMode.EXACT_START) {
            DateRange updateRange = timelinePlan.periodToUpdate();
            WorkCenter replaced = new WorkCenter(
                    coveringPeriod.getId(),
                    employeeId,
                    coveringPeriod.getWorkCenterAssignmentNumber(),
                    workCenterCode,
                    updateRange.startDate(),
                    updateRange.endDate(),
                    coveringPeriod.getCreatedAt(),
                    coveringPeriod.getUpdatedAt()
            );

            List<WorkCenter> projected = replaceByAssignmentNumber(currentHistory, replaced);
            return new ReplacementPlan(replaced, null, replaced, projected);
        }

        DateRange updateRange = timelinePlan.periodToUpdate();
        DateRange insertRange = timelinePlan.periodToInsert();

        WorkCenter adjustedExisting = new WorkCenter(
                coveringPeriod.getId(),
                employeeId,
                coveringPeriod.getWorkCenterAssignmentNumber(),
                coveringPeriod.getWorkCenterCode(),
                updateRange.startDate(),
                updateRange.endDate(),
                coveringPeriod.getCreatedAt(),
                coveringPeriod.getUpdatedAt()
        );

        WorkCenter replacement = new WorkCenter(
                null,
                employeeId,
                nextAssignmentNumber,
                workCenterCode,
                insertRange.startDate(),
                insertRange.endDate(),
                null,
                null
        );

        List<WorkCenter> projected = replaceByAssignmentNumber(currentHistory, adjustedExisting);
        projected.add(replacement);
        projected.sort(Comparator.comparing(WorkCenter::getStartDate));

        return new ReplacementPlan(adjustedExisting, replacement, replacement, projected);
    }

    private int nextAssignmentNumber(Long employeeId) {
        return workCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(employeeId)
                .map(value -> value + 1)
                .orElse(1);
    }

    private List<DateRange> toDateRanges(List<WorkCenter> history) {
        return history.stream()
                .map(period -> new DateRange(period.getStartDate(), period.getEndDate()))
                .toList();
    }

    private WorkCenter getPeriodByIndex(List<WorkCenter> history, Integer index) {
        if (index == null || index < 0 || index >= history.size()) {
            throw new IllegalStateException("Invalid covering period index in replacement plan");
        }

        return history.get(index);
    }

    private List<WorkCenter> replaceByAssignmentNumber(
            List<WorkCenter> history,
            WorkCenter updated
    ) {
        List<WorkCenter> projected = new ArrayList<>(history.size());
        for (WorkCenter workCenter : history) {
            if (workCenter.getWorkCenterAssignmentNumber().equals(updated.getWorkCenterAssignmentNumber())) {
                projected.add(updated);
            } else {
                projected.add(workCenter);
            }
        }

        return projected;
    }

    private void validateNoOverlap(
            List<WorkCenter> projectedHistory,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<WorkCenter> sorted = projectedHistory.stream()
                .sorted(Comparator.comparing(WorkCenter::getStartDate))
                .toList();

        for (int index = 1; index < sorted.size(); index++) {
            WorkCenter previous = sorted.get(index - 1);
            WorkCenter current = sorted.get(index);
            LocalDate previousEnd = TemporalDates.effectiveEnd(previous.getEndDate());

            if (!current.getStartDate().isAfter(previousEnd)) {
                throw new WorkCenterOverlapException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                );
            }
        }
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
            WorkCenter periodToUpdate,
            WorkCenter periodToSave,
            WorkCenter resultPeriod,
            List<WorkCenter> projectedHistory
    ) {
    }
}