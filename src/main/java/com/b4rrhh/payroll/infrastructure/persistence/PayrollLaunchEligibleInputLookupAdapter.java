package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.employee.presence.infrastructure.persistence.PresenceEntity;
import com.b4rrhh.employee.presence.infrastructure.persistence.SpringDataPresenceRepository;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeBusinessKeyLookupSupport;
import com.b4rrhh.employee.working_time.application.port.EmployeeAgreementContext;
import com.b4rrhh.employee.working_time.infrastructure.persistence.EmployeeAgreementContextRepository;
import com.b4rrhh.employee.working_time.infrastructure.persistence.SpringDataWorkingTimeRepository;
import com.b4rrhh.employee.working_time.infrastructure.persistence.WorkingTimeEntity;
import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchEligibleInputLookupPort;
import com.b4rrhh.payroll.application.port.PayrollLaunchWorkingTimeWindowContext;
import com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository.EmployeeAgreementCategoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PayrollLaunchEligibleInputLookupAdapter implements PayrollLaunchEligibleInputLookupPort {

    private final EmployeeBusinessKeyLookupSupport employeeLookupSupport;
    private final SpringDataPresenceRepository presenceRepository;
    private final EmployeeAgreementContextRepository agreementContextRepository;
    private final EmployeeAgreementCategoryRepository agreementCategoryRepository;
    private final SpringDataWorkingTimeRepository workingTimeRepository;

    public PayrollLaunchEligibleInputLookupAdapter(
            EmployeeBusinessKeyLookupSupport employeeLookupSupport,
            SpringDataPresenceRepository presenceRepository,
            EmployeeAgreementContextRepository agreementContextRepository,
            EmployeeAgreementCategoryRepository agreementCategoryRepository,
            SpringDataWorkingTimeRepository workingTimeRepository
    ) {
        this.employeeLookupSupport = employeeLookupSupport;
        this.presenceRepository = presenceRepository;
        this.agreementContextRepository = agreementContextRepository;
        this.agreementCategoryRepository = agreementCategoryRepository;
        this.workingTimeRepository = workingTimeRepository;
    }

    @Override
    public Optional<PayrollLaunchEligibleInputContext> findByUnitAndPeriod(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer presenceNumber,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        Optional<Long> employeeIdOpt = employeeLookupSupport.findByBusinessKey(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                )
                .map(employee -> employee.getId());

        if (employeeIdOpt.isEmpty()) {
            return Optional.empty();
        }

        Long employeeId = employeeIdOpt.get();
        Optional<PresenceEntity> presenceOpt = presenceRepository.findByEmployeeIdAndPresenceNumber(employeeId, presenceNumber)
                .filter(presence -> isOverlapping(presence.getStartDate(), presence.getEndDate(), periodStart, periodEnd));

        if (presenceOpt.isEmpty()) {
            return Optional.empty();
        }

        PresenceEntity presence = presenceOpt.get();
        List<EmployeeAgreementContext> agreementContexts = agreementContextRepository.findLatestValidByEmployeeIdAndEffectiveDate(
                employeeId,
                periodEnd,
                PageRequest.of(0, 1)
        );
        String agreementCode = agreementContexts.isEmpty() ? null : agreementContexts.getFirst().agreementCode();

        List<String> categories = agreementCategoryRepository.findLatestValidByEmployeeIdAndEffectiveDate(
                employeeId,
                periodEnd,
                PageRequest.of(0, 1)
        );
        String agreementCategoryCode = categories.isEmpty() ? null : categories.getFirst();

        List<PayrollLaunchWorkingTimeWindowContext> windows = workingTimeRepository
                .findOverlappingByEmployeeIdAndPeriodOrdered(employeeId, periodStart, periodEnd)
                .stream()
                .map(this::toWorkingTimeWindow)
                .toList();

        return Optional.of(new PayrollLaunchEligibleInputContext(
                presence.getCompanyCode(),
                agreementCode,
                agreementCategoryCode,
                windows
        ));
    }

    private boolean isOverlapping(
            LocalDate sourceStart,
            LocalDate sourceEnd,
            LocalDate queryStart,
            LocalDate queryEnd
    ) {
        return !sourceStart.isAfter(queryEnd)
                && (sourceEnd == null || !sourceEnd.isBefore(queryStart));
    }

    private PayrollLaunchWorkingTimeWindowContext toWorkingTimeWindow(WorkingTimeEntity workingTime) {
        return new PayrollLaunchWorkingTimeWindowContext(
                workingTime.getStartDate(),
                workingTime.getEndDate(),
                workingTime.getWorkingTimePercentage()
        );
    }
}
