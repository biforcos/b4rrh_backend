package com.b4rrhh.employee.contract.application.service;

import com.b4rrhh.employee.contract.application.port.ContractPresenceConsistencyPort;
import com.b4rrhh.employee.contract.application.port.PresencePeriod;
import com.b4rrhh.employee.contract.domain.exception.ContractCoverageIncompleteException;
import com.b4rrhh.employee.contract.domain.exception.ContractOutsidePresencePeriodException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Component
public class ContractPresenceCoverageValidator {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final ContractPresenceConsistencyPort contractPresenceConsistencyPort;

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
        if (!contractPresenceConsistencyPort.existsPresenceContainingPeriod(employeeId, startDate, endDate)) {
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
        List<PresencePeriod> presencePeriods = contractPresenceConsistencyPort
                .findPresencePeriodsByEmployeeIdOrderByStartDate(employeeId)
                .stream()
                .sorted(Comparator.comparing(PresencePeriod::startDate))
                .toList();

        List<Contract> sortedContracts = projectedContractHistory
                .stream()
                .sorted(Comparator.comparing(Contract::getStartDate))
                .toList();

        for (Contract contract : sortedContracts) {
            validatePeriodWithinPresence(
                    employeeId,
                    contract.getStartDate(),
                    contract.getEndDate(),
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber
            );
        }

        if (!isFullyCovered(presencePeriods, sortedContracts)) {
            throw new ContractCoverageIncompleteException(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber
            );
        }
    }

    private boolean isFullyCovered(
            List<PresencePeriod> presencePeriods,
            List<Contract> contracts
    ) {
        if (presencePeriods.isEmpty()) {
            return contracts.isEmpty();
        }

        for (PresencePeriod presencePeriod : presencePeriods) {
            LocalDate presenceStart = presencePeriod.startDate();
            LocalDate presenceEnd = normalizeEndDate(presencePeriod.endDate());
            LocalDate cursor = presenceStart;

            for (Contract contract : contracts) {
                LocalDate contractStart = contract.getStartDate();
                LocalDate contractEnd = normalizeEndDate(contract.getEndDate());

                if (contractEnd.isBefore(presenceStart) || contractStart.isAfter(presenceEnd)) {
                    continue;
                }

                LocalDate effectiveStart = contractStart.isBefore(presenceStart)
                        ? presenceStart
                    : contractStart;
                LocalDate effectiveEnd = contractEnd.isAfter(presenceEnd)
                        ? presenceEnd
                    : contractEnd;

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
