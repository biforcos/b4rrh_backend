package com.b4rrhh.employee.contract.application.service;

import com.b4rrhh.employee.contract.application.port.ContractPresenceConsistencyPort;
import com.b4rrhh.employee.contract.application.port.PresencePeriod;
import com.b4rrhh.employee.contract.domain.exception.ContractCoverageIncompleteException;
import com.b4rrhh.employee.contract.domain.exception.ContractOutsidePresencePeriodException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.temporal.support.DateRange;
import com.b4rrhh.employee.temporal.support.TimelineCoverageValidator;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContractPresenceCoverageValidator {

    private final ContractPresenceConsistencyPort contractPresenceConsistencyPort;
    private final TimelineCoverageValidator timelineCoverageValidator = new TimelineCoverageValidator();

    public ContractPresenceCoverageValidator(
            ContractPresenceConsistencyPort contractPresenceConsistencyPort
    ) {
        this.contractPresenceConsistencyPort = contractPresenceConsistencyPort;
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
            throw new ContractOutsidePresencePeriodException(
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
            List<Contract> projectedContractHistory,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<DateRange> presenceRanges = toPresenceRanges(employeeId);
        List<DateRange> contractRanges = projectedContractHistory
                .stream()
                .map(contract -> new DateRange(contract.getStartDate(), contract.getEndDate()))
                .toList();

        for (DateRange contractRange : contractRanges) {
            if (!timelineCoverageValidator.isContained(List.of(contractRange), presenceRanges)) {
                throw new ContractOutsidePresencePeriodException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        contractRange.startDate(),
                        contractRange.endDate()
                );
            }
        }

        if (!timelineCoverageValidator.isFullyCovered(contractRanges, presenceRanges)) {
            throw new ContractCoverageIncompleteException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber
            );
        }
    }

    private List<DateRange> toPresenceRanges(Long employeeId) {
        return contractPresenceConsistencyPort
                .findPresencePeriodsByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .map(this::toDateRange)
                .toList();
    }

    private DateRange toDateRange(PresencePeriod presencePeriod) {
        return new DateRange(presencePeriod.startDate(), presencePeriod.endDate());
    }
}
