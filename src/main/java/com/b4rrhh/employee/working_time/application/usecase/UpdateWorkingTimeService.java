package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.AgreementAnnualHoursLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContextLookupPort;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.WorkingTimePresenceConsistencyValidator;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeAlreadyClosedException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeEmployeeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOverlapException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import com.b4rrhh.employee.working_time.domain.service.WorkingTimeDerivationPolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class UpdateWorkingTimeService implements UpdateWorkingTimeUseCase {

    private final WorkingTimeRepository workingTimeRepository;
    private final EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;
    private final EmployeeAgreementContextLookupPort employeeAgreementContextLookupPort;
    private final AgreementAnnualHoursLookupPort agreementAnnualHoursLookupPort;
    private final WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator;
    private final WorkingTimeDerivationPolicy workingTimeDerivationPolicy;

    public UpdateWorkingTimeService(
            WorkingTimeRepository workingTimeRepository,
            EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort,
            EmployeeAgreementContextLookupPort employeeAgreementContextLookupPort,
            AgreementAnnualHoursLookupPort agreementAnnualHoursLookupPort,
            WorkingTimePresenceConsistencyValidator workingTimePresenceConsistencyValidator,
            WorkingTimeDerivationPolicy workingTimeDerivationPolicy
    ) {
        this.workingTimeRepository = workingTimeRepository;
        this.employeeWorkingTimeLookupPort = employeeWorkingTimeLookupPort;
        this.employeeAgreementContextLookupPort = employeeAgreementContextLookupPort;
        this.agreementAnnualHoursLookupPort = agreementAnnualHoursLookupPort;
        this.workingTimePresenceConsistencyValidator = workingTimePresenceConsistencyValidator;
        this.workingTimeDerivationPolicy = workingTimeDerivationPolicy;
    }

    @Override
    @Transactional
    public WorkingTime update(UpdateWorkingTimeCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        Integer normalizedWorkingTimeNumber = normalizeWorkingTimeNumber(command.workingTimeNumber());
        LocalDate normalizedStartDate = normalizeStartDate(command.startDate());
        BigDecimal normalizedPercentage = normalizeWorkingTimePercentage(command.workingTimePercentage());

        EmployeeWorkingTimeContext employee = employeeWorkingTimeLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new WorkingTimeEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        WorkingTime existing = workingTimeRepository
                .findByEmployeeIdAndWorkingTimeNumber(employee.employeeId(), normalizedWorkingTimeNumber)
                .orElseThrow(() -> new WorkingTimeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedWorkingTimeNumber
                ));

        if (!existing.isActive()) {
            throw new WorkingTimeAlreadyClosedException(normalizedWorkingTimeNumber);
        }

        // Resolve agreement context at the new startDate to compute derived hours
        EmployeeAgreementContext agreementContext = employeeAgreementContextLookupPort
                .resolveContext(employee.employeeId(), normalizedStartDate);

        BigDecimal annualHours = agreementAnnualHoursLookupPort.resolveAnnualHours(
                agreementContext.ruleSystemCode(),
                agreementContext.agreementCode()
        );

        WorkingTimeDerivedHours derivedHours = workingTimeDerivationPolicy.derive(normalizedPercentage, annualHours);

        WorkingTime updated = WorkingTime.rehydrate(
                existing.getId(),
                existing.getEmployeeId(),
                existing.getWorkingTimeNumber(),
                normalizedStartDate,
                existing.getEndDate(),
                normalizedPercentage,
                derivedHours,
                existing.getCreatedAt(),
                null
        );

        List<WorkingTime> fullHistory = workingTimeRepository.findByEmployeeIdOrderByStartDate(employee.employeeId());

        if (!normalizedStartDate.equals(existing.getStartDate())) {
            LocalDate expectedPredecessorEnd = existing.getStartDate().minusDays(1);
            WorkingTime predecessor = fullHistory.stream()
                    .filter(wt -> expectedPredecessorEnd.equals(wt.getEndDate()))
                    .findFirst()
                    .orElse(null);
            if (predecessor != null && normalizedStartDate.isAfter(predecessor.getStartDate())) {
                WorkingTime cascadedPredecessor = predecessor.adjustEndDate(normalizedStartDate.minusDays(1));
                workingTimeRepository.save(cascadedPredecessor);
            }
        }

        if (workingTimeRepository.existsOverlappingPeriodExcluding(
                employee.employeeId(),
                updated.getStartDate(),
                updated.getEndDate(),
                normalizedWorkingTimeNumber
        )) {
            throw new WorkingTimeOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    updated.getStartDate(),
                    updated.getEndDate()
            );
        }

        workingTimePresenceConsistencyValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                updated.getStartDate(),
                updated.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        return workingTimeRepository.save(updated);
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

    private Integer normalizeWorkingTimeNumber(Integer workingTimeNumber) {
        if (workingTimeNumber == null || workingTimeNumber <= 0) {
            throw new IllegalArgumentException("workingTimeNumber must be a positive integer");
        }
        return workingTimeNumber;
    }

    private LocalDate normalizeStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }
        return startDate;
    }

    private BigDecimal normalizeWorkingTimePercentage(BigDecimal percentage) {
        if (percentage == null) {
            throw new IllegalArgumentException("workingTimePercentage is required");
        }
        return percentage;
    }
}
