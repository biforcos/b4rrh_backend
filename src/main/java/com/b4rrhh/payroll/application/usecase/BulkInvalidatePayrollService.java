package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.application.port.PayrollLaunchEmployeeContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchPresenceContext;
import com.b4rrhh.payroll.application.port.PayrollLaunchPresenceLookupPort;
import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import com.b4rrhh.payroll.domain.port.PayrollRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

@Service
public class BulkInvalidatePayrollService implements BulkInvalidatePayrollUseCase {

    private static final DateTimeFormatter PAYROLL_PERIOD_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final PayrollRepository payrollRepository;
    private final PayrollLaunchPresenceLookupPort payrollLaunchPresenceLookupPort;

    public BulkInvalidatePayrollService(
            PayrollRepository payrollRepository,
            PayrollLaunchPresenceLookupPort payrollLaunchPresenceLookupPort
    ) {
        this.payrollRepository = payrollRepository;
        this.payrollLaunchPresenceLookupPort = payrollLaunchPresenceLookupPort;
    }

    @Override
    @Transactional
    public BulkInvalidatePayrollResult invalidateBulk(BulkInvalidatePayrollCommand command) {
        String ruleSystemCode = normalizeCode(command.ruleSystemCode(), "ruleSystemCode", 5);
        String payrollPeriodCode = normalizeCode(command.payrollPeriodCode(), "payrollPeriodCode", 30);
        String payrollTypeCode = normalizeCode(command.payrollTypeCode(), "payrollTypeCode", 30);
        String statusReasonCode = normalizeCode(command.statusReasonCode(), "statusReasonCode", 50);
        PayrollLaunchTargetSelection targetSelection = normalizeTargetSelection(command.targetSelection());
        LocalDate[] periodBounds = parsePayrollPeriodBounds(payrollPeriodCode);
        LocalDate periodStart = periodBounds[0];
        LocalDate periodEnd = periodBounds[1];

        List<PayrollCalculationUnit> candidates = expandCandidates(
                targetSelection, ruleSystemCode, payrollPeriodCode, payrollTypeCode, periodStart, periodEnd
        );

        int totalFound = 0;
        int totalInvalidated = 0;
        int totalSkippedAlreadyNotValid = 0;
        int totalSkippedProtected = 0;
        int totalSkippedNotFound = 0;

        for (PayrollCalculationUnit unit : candidates) {
            Optional<Payroll> found = payrollRepository.findByBusinessKey(
                    unit.ruleSystemCode(),
                    unit.employeeTypeCode(),
                    unit.employeeNumber(),
                    unit.payrollPeriodCode(),
                    unit.payrollTypeCode(),
                    unit.presenceNumber()
            );

            if (found.isEmpty()) {
                totalSkippedNotFound++;
                continue;
            }

            totalFound++;
            Payroll existing = found.get();

            if (existing.getStatus() == PayrollStatus.CALCULATED) {
                Payroll invalidated = existing.invalidate(statusReasonCode);
                payrollRepository.save(invalidated);
                totalInvalidated++;
            } else if (existing.getStatus() == PayrollStatus.NOT_VALID) {
                totalSkippedAlreadyNotValid++;
            } else {
                // EXPLICIT_VALIDATED or DEFINITIVE — protected, must not be bulk-invalidated
                totalSkippedProtected++;
            }
        }

        return new BulkInvalidatePayrollResult(
                ruleSystemCode,
                payrollPeriodCode,
                payrollTypeCode,
                candidates.size(),
                totalFound,
                totalInvalidated,
                totalSkippedAlreadyNotValid,
                totalSkippedProtected,
                totalSkippedNotFound,
                statusReasonCode
        );
    }

    private List<PayrollCalculationUnit> expandCandidates(
            PayrollLaunchTargetSelection targetSelection,
            String ruleSystemCode,
            String payrollPeriodCode,
            String payrollTypeCode,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        List<PayrollLaunchEmployeeTarget> employeeTargets = resolveEmployees(
                targetSelection, ruleSystemCode, periodStart, periodEnd
        );

        List<PayrollCalculationUnit> units = new ArrayList<>();
        for (PayrollLaunchEmployeeTarget employeeTarget : employeeTargets) {
            List<PayrollLaunchPresenceContext> presences = payrollLaunchPresenceLookupPort.findRelevantPresences(
                    ruleSystemCode,
                    employeeTarget.employeeTypeCode(),
                    employeeTarget.employeeNumber(),
                    periodStart,
                    periodEnd
            );
            for (PayrollLaunchPresenceContext presence : presences) {
                units.add(new PayrollCalculationUnit(
                        ruleSystemCode,
                        presence.employeeTypeCode(),
                        presence.employeeNumber(),
                        payrollPeriodCode,
                        payrollTypeCode,
                        presence.presenceNumber()
                ));
            }
        }
        return units;
    }

    private List<PayrollLaunchEmployeeTarget> resolveEmployees(
            PayrollLaunchTargetSelection targetSelection,
            String ruleSystemCode,
            LocalDate periodStart,
            LocalDate periodEnd
    ) {
        List<PayrollLaunchEmployeeTarget> rawTargets = switch (targetSelection.selectionType()) {
            case SINGLE_EMPLOYEE -> List.of(targetSelection.employee());
            case EMPLOYEE_LIST -> targetSelection.employees();
            case ALL_EMPLOYEES_WITH_PRESENCE_IN_PERIOD -> payrollLaunchPresenceLookupPort
                    .findEmployeesWithPresenceInPeriod(ruleSystemCode, periodStart, periodEnd)
                    .stream()
                    .map(emp -> new PayrollLaunchEmployeeTarget(emp.employeeTypeCode(), emp.employeeNumber()))
                    .toList();
        };

        LinkedHashMap<String, PayrollLaunchEmployeeTarget> uniqueTargets = new LinkedHashMap<>();
        for (PayrollLaunchEmployeeTarget rawTarget : rawTargets) {
            String employeeTypeCode = normalizeCode(rawTarget.employeeTypeCode(), "targetSelection.employeeTypeCode", 30);
            String employeeNumber = normalizeText(rawTarget.employeeNumber(), "targetSelection.employeeNumber", 15);
            PayrollLaunchEmployeeTarget normalizedTarget = new PayrollLaunchEmployeeTarget(employeeTypeCode, employeeNumber);
            uniqueTargets.put(employeeTypeCode + "|" + employeeNumber, normalizedTarget);
        }
        return List.copyOf(uniqueTargets.values());
    }

    private PayrollLaunchTargetSelection normalizeTargetSelection(PayrollLaunchTargetSelection targetSelection) {
        if (targetSelection == null || targetSelection.selectionType() == null) {
            throw new InvalidPayrollArgumentException("targetSelection.selectionType is required");
        }

        return switch (targetSelection.selectionType()) {
            case SINGLE_EMPLOYEE -> {
                if (targetSelection.employee() == null) {
                    throw new InvalidPayrollArgumentException("targetSelection.employee is required for SINGLE_EMPLOYEE");
                }
                yield new PayrollLaunchTargetSelection(
                        PayrollLaunchTargetSelectionType.SINGLE_EMPLOYEE,
                        targetSelection.employee(),
                        null
                );
            }
            case EMPLOYEE_LIST -> {
                if (targetSelection.employees() == null || targetSelection.employees().isEmpty()) {
                    throw new InvalidPayrollArgumentException("targetSelection.employees is required for EMPLOYEE_LIST");
                }
                yield new PayrollLaunchTargetSelection(
                        PayrollLaunchTargetSelectionType.EMPLOYEE_LIST,
                        null,
                        List.copyOf(targetSelection.employees())
                );
            }
            case ALL_EMPLOYEES_WITH_PRESENCE_IN_PERIOD -> {
                if (targetSelection.employee() != null || targetSelection.employees() != null) {
                    throw new InvalidPayrollArgumentException(
                            "targetSelection.employee and targetSelection.employees must be null for ALL_EMPLOYEES_WITH_PRESENCE_IN_PERIOD"
                    );
                }
                yield new PayrollLaunchTargetSelection(
                        PayrollLaunchTargetSelectionType.ALL_EMPLOYEES_WITH_PRESENCE_IN_PERIOD,
                        null,
                        null
                );
            }
        };
    }

    private LocalDate[] parsePayrollPeriodBounds(String payrollPeriodCode) {
        try {
            YearMonth yearMonth = YearMonth.parse(payrollPeriodCode, PAYROLL_PERIOD_FORMATTER);
            return new LocalDate[]{yearMonth.atDay(1), yearMonth.atEndOfMonth()};
        } catch (DateTimeParseException ex) {
            throw new InvalidPayrollArgumentException("payrollPeriodCode must be in yyyyMM format, got: " + payrollPeriodCode);
        }
    }

    private String normalizeCode(String value, String fieldName, int maxLength) {
        return normalizeText(value, fieldName, maxLength).toUpperCase();
    }

    private String normalizeText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPayrollArgumentException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new InvalidPayrollArgumentException(fieldName + " exceeds max length " + maxLength);
        }
        return normalized;
    }
}
