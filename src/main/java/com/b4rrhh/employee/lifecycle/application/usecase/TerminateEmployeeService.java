package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.usecase.CloseContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractAlreadyClosedException;
import com.b4rrhh.employee.contract.domain.exception.ContractCoverageIncompleteException;
import com.b4rrhh.employee.contract.domain.exception.ContractNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractOutsidePresencePeriodException;
import com.b4rrhh.employee.contract.domain.exception.InvalidContractDateRangeException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.employee.application.usecase.GetEmployeeByBusinessKeyUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.labor_classification.application.command.CloseLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CloseLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.InvalidLaborClassificationDateRangeException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAlreadyClosedException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCoverageIncompleteException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationOutsidePresencePeriodException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminateEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeEmployeeNotFoundException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeRequestInvalidException;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.domain.exception.InvalidPresenceDateRangeException;
import com.b4rrhh.employee.presence.domain.exception.PresenceAlreadyClosedException;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.employee.presence.domain.exception.PresenceNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.cost_center.application.usecase.CloseActiveCostCenterDistributionAtTerminationUseCase;
import com.b4rrhh.employee.working_time.application.usecase.CloseWorkingTimeCommand;
import com.b4rrhh.employee.working_time.application.usecase.CloseWorkingTimeUseCase;
import com.b4rrhh.employee.working_time.application.usecase.ListEmployeeWorkingTimesCommand;
import com.b4rrhh.employee.working_time.application.usecase.ListEmployeeWorkingTimesUseCase;
import com.b4rrhh.employee.working_time.domain.exception.InvalidWorkingTimeDateRangeException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeAlreadyClosedException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeNotFoundException;
import com.b4rrhh.employee.working_time.domain.exception.WorkingTimeOutsidePresencePeriodException;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.InvalidWorkCenterDateRangeException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterAlreadyClosedException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterNotFoundException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterPresenceCoverageGapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

@Service
public class TerminateEmployeeService implements TerminateEmployeeUseCase {

    private static final String TERMINATED_STATUS = "TERMINATED";

    private final GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase;
    private final EmployeeRepository employeeRepository;
    private final ListEmployeePresencesUseCase listEmployeePresencesUseCase;
    private final ListEmployeeContractsUseCase listEmployeeContractsUseCase;
    private final ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase;
    private final ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase;
        private final ListEmployeeWorkingTimesUseCase listEmployeeWorkingTimesUseCase;
    private final CloseWorkCenterUseCase closeWorkCenterUseCase;
    private final CloseLaborClassificationUseCase closeLaborClassificationUseCase;
    private final CloseContractUseCase closeContractUseCase;
    private final ClosePresenceUseCase closePresenceUseCase;
        private final CloseWorkingTimeUseCase closeWorkingTimeUseCase;
    private final CloseActiveCostCenterDistributionAtTerminationUseCase closeActiveCostCenterDistributionUseCase;

    public TerminateEmployeeService(
            GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase,
            EmployeeRepository employeeRepository,
            ListEmployeePresencesUseCase listEmployeePresencesUseCase,
            ListEmployeeContractsUseCase listEmployeeContractsUseCase,
            ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase,
            ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase,
            ListEmployeeWorkingTimesUseCase listEmployeeWorkingTimesUseCase,
            CloseWorkCenterUseCase closeWorkCenterUseCase,
            CloseLaborClassificationUseCase closeLaborClassificationUseCase,
            CloseContractUseCase closeContractUseCase,
            ClosePresenceUseCase closePresenceUseCase,
            CloseWorkingTimeUseCase closeWorkingTimeUseCase,
            CloseActiveCostCenterDistributionAtTerminationUseCase closeActiveCostCenterDistributionUseCase
    ) {
        this.getEmployeeByBusinessKeyUseCase = getEmployeeByBusinessKeyUseCase;
        this.employeeRepository = employeeRepository;
        this.listEmployeePresencesUseCase = listEmployeePresencesUseCase;
        this.listEmployeeContractsUseCase = listEmployeeContractsUseCase;
        this.listEmployeeLaborClassificationsUseCase = listEmployeeLaborClassificationsUseCase;
        this.listEmployeeWorkCentersUseCase = listEmployeeWorkCentersUseCase;
                this.listEmployeeWorkingTimesUseCase = listEmployeeWorkingTimesUseCase;
        this.closeWorkCenterUseCase = closeWorkCenterUseCase;
        this.closeLaborClassificationUseCase = closeLaborClassificationUseCase;
        this.closeContractUseCase = closeContractUseCase;
        this.closePresenceUseCase = closePresenceUseCase;
                this.closeWorkingTimeUseCase = closeWorkingTimeUseCase;
        this.closeActiveCostCenterDistributionUseCase = closeActiveCostCenterDistributionUseCase;
    }

    @Override
    @Transactional
    public TerminateEmployeeResult terminate(TerminateEmployeeCommand command) {
        if (command == null) {
            throw new TerminateEmployeeRequestInvalidException("request body is required");
        }

        String ruleSystemCode = normalizeRequiredCode("ruleSystemCode", command.ruleSystemCode(), false);
        String employeeTypeCode = normalizeRequiredCode("employeeTypeCode", command.employeeTypeCode(), false);
        String employeeNumber = normalizeRequiredText("employeeNumber", command.employeeNumber());
        LocalDate terminationDate = normalizeRequiredDate(command.terminationDate());
        String exitReasonCode = normalizeRequiredCode("exitReasonCode", command.exitReasonCode(), false);

        Employee employee = getEmployeeByBusinessKeyUseCase
                .getByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .orElseThrow(() -> new TerminateEmployeeEmployeeNotFoundException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                ));

        List<Presence> presenceHistory = listEmployeePresencesUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber);
        List<Contract> contractHistory = listEmployeeContractsUseCase.listByEmployeeBusinessKey(
                new ListEmployeeContractsCommand(ruleSystemCode, employeeTypeCode, employeeNumber)
        );
        List<LaborClassification> laborHistory = listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(
                new ListEmployeeLaborClassificationsCommand(ruleSystemCode, employeeTypeCode, employeeNumber)
        );
        List<WorkCenter> workCenterHistory = listEmployeeWorkCentersUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber);
        List<WorkingTime> workingTimeHistory = listEmployeeWorkingTimesUseCase.listByEmployeeBusinessKey(
                new ListEmployeeWorkingTimesCommand(ruleSystemCode, employeeTypeCode, employeeNumber)
        );

        if (TERMINATED_STATUS.equalsIgnoreCase(employee.getStatus())) {
            return resolveIdempotentTermination(
                    employee,
                    terminationDate,
                    exitReasonCode,
                    presenceHistory,
                    contractHistory,
                    laborHistory,
                                        workCenterHistory,
                                        workingTimeHistory
            );
        }

        Presence activePresence = requireExactlyOneActive(
                "presence",
                presenceHistory,
                Presence::isActive
        );

        List<Contract> activeContracts = requireAtMostOneActive(
                "contract",
                contractHistory,
                Contract::isActive
        );
        Optional<Contract> activeContract = resolveOptionalActiveInTerminationDate(
                "contract",
                activeContracts,
                Contract::getStartDate,
                terminationDate
        );

        List<LaborClassification> activeLaborClassifications = requireAtMostOneActive(
                "labor classification",
                laborHistory,
                LaborClassification::isActive
        );
        Optional<LaborClassification> activeLaborClassification = resolveOptionalActiveInTerminationDate(
                "labor classification",
                activeLaborClassifications,
                LaborClassification::getStartDate,
                terminationDate
        );

        List<WorkCenter> activeWorkCenters = requireAtMostOneActive(
                "work center",
                workCenterHistory,
                WorkCenter::isActive
        );
        Optional<WorkCenter> activeWorkCenter = resolveOptionalActiveInTerminationDate(
                "work center",
                activeWorkCenters,
                WorkCenter::getStartDate,
                terminationDate
        );

        List<WorkingTime> activeWorkingTimes = requireAtMostOneActive(
                "working time",
                workingTimeHistory,
                WorkingTime::isActive
        );
        Optional<WorkingTime> activeWorkingTime = resolveOptionalActiveInTerminationDate(
                "working time",
                activeWorkingTimes,
                WorkingTime::getStartDate,
                terminationDate
        );

        validateTerminationDateNotBeforeStart("active presence", activePresence.getStartDate(), terminationDate);
        activeContract.ifPresent(contract ->
                validateTerminationDateNotBeforeStart("active contract", contract.getStartDate(), terminationDate)
        );
        activeLaborClassification.ifPresent(laborClassification ->
                validateTerminationDateNotBeforeStart(
                        "active labor classification",
                        laborClassification.getStartDate(),
                        terminationDate
                )
        );
        activeWorkCenter.ifPresent(workCenter ->
                validateTerminationDateNotBeforeStart("active work center", workCenter.getStartDate(), terminationDate)
        );
        activeWorkingTime.ifPresent(workingTime ->
                validateTerminationDateNotBeforeStart("active working time", workingTime.getStartDate(), terminationDate)
        );

        WorkCenter closedWorkCenter = null;
        LaborClassification closedLaborClassification = null;
        Contract closedContract = null;
        WorkingTime closedWorkingTime = null;
        Presence closedPresence;

        try {
                        closedPresence = closePresenceUseCase.close(new ClosePresenceCommand(
                                        ruleSystemCode,
                                        employeeTypeCode,
                                        employeeNumber,
                                        activePresence.getPresenceNumber(),
                                        terminationDate,
                                        exitReasonCode
                        ));

            if (activeWorkCenter.isPresent()) {
                WorkCenter workCenter = activeWorkCenter.get();
                closedWorkCenter = closeWorkCenterUseCase.close(new CloseWorkCenterCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        workCenter.getWorkCenterAssignmentNumber(),
                        terminationDate
                ));
            }

            if (activeLaborClassification.isPresent()) {
                LaborClassification laborClassification = activeLaborClassification.get();
                closedLaborClassification = closeLaborClassificationUseCase.close(new CloseLaborClassificationCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        laborClassification.getStartDate(),
                        terminationDate
                ));
            }

            if (activeContract.isPresent()) {
                Contract contract = activeContract.get();
                closedContract = closeContractUseCase.close(new CloseContractCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        contract.getStartDate(),
                        terminationDate
                ));
            }

            if (activeWorkingTime.isPresent()) {
                WorkingTime workingTime = activeWorkingTime.get();
                closedWorkingTime = closeWorkingTimeUseCase.close(new CloseWorkingTimeCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        workingTime.getWorkingTimeNumber(),
                        terminationDate
                ));
            }

            closeActiveCostCenterDistributionUseCase.closeIfPresent(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    terminationDate
            );
        } catch (PresenceCatalogValueInvalidException ex) {
            throw new TerminateEmployeeCatalogValueInvalidException(ex.getMessage(), ex);
        } catch (PresenceNotFoundException
                 | PresenceAlreadyClosedException
                 | InvalidPresenceDateRangeException
                 | ContractNotFoundException
                 | ContractAlreadyClosedException
                 | InvalidContractDateRangeException
                 | ContractOutsidePresencePeriodException
                 | ContractCoverageIncompleteException
                 | LaborClassificationNotFoundException
                 | LaborClassificationAlreadyClosedException
                 | InvalidLaborClassificationDateRangeException
                 | LaborClassificationOutsidePresencePeriodException
                 | LaborClassificationCoverageIncompleteException
                 | WorkCenterNotFoundException
                 | WorkCenterAlreadyClosedException
                 | InvalidWorkCenterDateRangeException
                 | WorkCenterOutsidePresencePeriodException
                                 | WorkCenterPresenceCoverageGapException
                                 | WorkingTimeNotFoundException
                                 | WorkingTimeAlreadyClosedException
                                 | InvalidWorkingTimeDateRangeException
                                 | WorkingTimeOutsidePresencePeriodException ex) {
            throw new TerminateEmployeeConflictException(ex.getMessage(), ex);
        }

        Employee terminatedEmployee = employeeRepository.save(new Employee(
                employee.getId(),
                employee.getRuleSystemCode(),
                employee.getEmployeeTypeCode(),
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName1(),
                employee.getLastName2(),
                employee.getPreferredName(),
                TERMINATED_STATUS,
                employee.getCreatedAt(),
                LocalDateTime.now(),
                employee.getPhotoUrl()
        ));

        List<Presence> presencesAfterClose = listEmployeePresencesUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber);

        // Deduplicate by presenceNumber, preferring closed occurrences when duplicates exist
        java.util.Map<Integer, Presence> dedup = new java.util.HashMap<>();
        for (Presence p : presencesAfterClose) {
            Integer num = p.getPresenceNumber();
            Presence existing = dedup.get(num);
            if (existing == null) {
                dedup.put(num, p);
            } else {
                // prefer a closed occurrence over an active one
                if (existing.getEndDate() == null && p.getEndDate() != null) {
                    dedup.put(num, p);
                } else if (existing.getEndDate() != null && p.getEndDate() != null) {
                    if (p.getEndDate().isAfter(existing.getEndDate())) {
                        dedup.put(num, p);
                    }
                }
                // otherwise keep existing (either both active or existing already closed)
            }
        }

        long activePresenceCountAfterClose = dedup.values().stream().filter(Presence::isActive).count();

        if (activePresenceCountAfterClose != 0) {
            throw new TerminateEmployeeConflictException(
                    "Termination post-condition failed: active presence remains after close"
            );
        }

        return new TerminateEmployeeResult(
                terminatedEmployee.getRuleSystemCode(),
                terminatedEmployee.getEmployeeTypeCode(),
                terminatedEmployee.getEmployeeNumber(),
                terminationDate,
                exitReasonCode,
                terminatedEmployee.getStatus(),
                closedPresence.getPresenceNumber(),
                closedPresence.getCompanyCode(),
                closedPresence.getEntryReasonCode(),
                closedPresence.getExitReasonCode(),
                closedPresence.getStartDate(),
                                closedPresence.getEndDate(),
                                closedContract != null ? closedContract.getContractCode() : null,
                                closedContract != null ? closedContract.getContractSubtypeCode() : null,
                                closedContract != null ? closedContract.getStartDate() : null,
                                closedContract != null ? closedContract.getEndDate() : null,
                                closedLaborClassification != null ? closedLaborClassification.getAgreementCode() : null,
                                closedLaborClassification != null ? closedLaborClassification.getAgreementCategoryCode() : null,
                                closedLaborClassification != null ? closedLaborClassification.getStartDate() : null,
                                closedLaborClassification != null ? closedLaborClassification.getEndDate() : null,
                                closedWorkCenter != null ? closedWorkCenter.getWorkCenterAssignmentNumber() : null,
                                closedWorkCenter != null ? closedWorkCenter.getWorkCenterCode() : null,
                                closedWorkCenter != null ? closedWorkCenter.getStartDate() : null,
                                closedWorkCenter != null ? closedWorkCenter.getEndDate() : null,
                                closedWorkingTime != null ? closedWorkingTime.getWorkingTimeNumber() : null,
                                closedWorkingTime != null ? closedWorkingTime.getWorkingTimePercentage() : null,
                                closedWorkingTime != null ? closedWorkingTime.getWeeklyHours() : null,
                                closedWorkingTime != null ? closedWorkingTime.getDailyHours() : null,
                                closedWorkingTime != null ? closedWorkingTime.getMonthlyHours() : null,
                                closedWorkingTime != null ? closedWorkingTime.getStartDate() : null,
                                closedWorkingTime != null ? closedWorkingTime.getEndDate() : null
        );
    }

    private TerminateEmployeeResult resolveIdempotentTermination(
            Employee employee,
            LocalDate terminationDate,
            String exitReasonCode,
            List<Presence> presenceHistory,
            List<Contract> contractHistory,
            List<LaborClassification> laborHistory,
            List<WorkCenter> workCenterHistory,
            List<WorkingTime> workingTimeHistory
    ) {
        Presence idempotentPresence = presenceHistory.stream()
                .filter(presence -> terminationDate.equals(presence.getEndDate()))
                .filter(presence -> exitReasonCode.equals(presence.getExitReasonCode()))
                .max(Comparator.comparing(Presence::getStartDate)
                        .thenComparing(Presence::getPresenceNumber))
                .orElseThrow(() -> new TerminateEmployeeConflictException(
                        "Employee already TERMINATED but no closed presence matches terminationDate and exitReasonCode"
                ));

        Contract closedContract = contractHistory.stream()
                .filter(contract -> terminationDate.equals(contract.getEndDate()))
                .max(Comparator.comparing(Contract::getStartDate))
                .orElse(null);

        LaborClassification closedLabor = laborHistory.stream()
                .filter(labor -> terminationDate.equals(labor.getEndDate()))
                .max(Comparator.comparing(LaborClassification::getStartDate))
                .orElse(null);

        WorkCenter closedWorkCenter = workCenterHistory.stream()
                .filter(workCenter -> terminationDate.equals(workCenter.getEndDate()))
                .max(Comparator.comparing(WorkCenter::getStartDate)
                        .thenComparing(WorkCenter::getWorkCenterAssignmentNumber))
                .orElse(null);

        WorkingTime closedWorkingTime = workingTimeHistory.stream()
                .filter(workingTime -> terminationDate.equals(workingTime.getEndDate()))
                .max(Comparator.comparing(WorkingTime::getStartDate)
                        .thenComparing(WorkingTime::getWorkingTimeNumber))
                .orElse(null);

        return new TerminateEmployeeResult(
                employee.getRuleSystemCode(),
                employee.getEmployeeTypeCode(),
                employee.getEmployeeNumber(),
                terminationDate,
                exitReasonCode,
                TERMINATED_STATUS,
                idempotentPresence.getPresenceNumber(),
                idempotentPresence.getCompanyCode(),
                idempotentPresence.getEntryReasonCode(),
                idempotentPresence.getExitReasonCode(),
                idempotentPresence.getStartDate(),
                idempotentPresence.getEndDate(),
                closedContract != null ? closedContract.getContractCode() : null,
                closedContract != null ? closedContract.getContractSubtypeCode() : null,
                closedContract != null ? closedContract.getStartDate() : null,
                closedContract != null ? closedContract.getEndDate() : null,
                closedLabor != null ? closedLabor.getAgreementCode() : null,
                closedLabor != null ? closedLabor.getAgreementCategoryCode() : null,
                closedLabor != null ? closedLabor.getStartDate() : null,
                closedLabor != null ? closedLabor.getEndDate() : null,
                closedWorkCenter != null ? closedWorkCenter.getWorkCenterAssignmentNumber() : null,
                closedWorkCenter != null ? closedWorkCenter.getWorkCenterCode() : null,
                closedWorkCenter != null ? closedWorkCenter.getStartDate() : null,
                                closedWorkCenter != null ? closedWorkCenter.getEndDate() : null,
                                closedWorkingTime != null ? closedWorkingTime.getWorkingTimeNumber() : null,
                                closedWorkingTime != null ? closedWorkingTime.getWorkingTimePercentage() : null,
                                closedWorkingTime != null ? closedWorkingTime.getWeeklyHours() : null,
                                closedWorkingTime != null ? closedWorkingTime.getDailyHours() : null,
                                closedWorkingTime != null ? closedWorkingTime.getMonthlyHours() : null,
                                closedWorkingTime != null ? closedWorkingTime.getStartDate() : null,
                                closedWorkingTime != null ? closedWorkingTime.getEndDate() : null
        );
    }

    private <T> T requireExactlyOneActive(String label, List<T> occurrences, Predicate<T> isActive) {
        List<T> active = occurrences.stream().filter(isActive).toList();

        if (active.size() != 1) {
            throw new TerminateEmployeeConflictException(
                    "Expected exactly one active " + label + " but found " + active.size()
            );
        }

        return active.get(0);
    }

        private <T> List<T> requireAtMostOneActive(String label, List<T> occurrences, Predicate<T> isActive) {
                List<T> active = occurrences.stream().filter(isActive).toList();

                if (active.size() > 1) {
                        throw new TerminateEmployeeConflictException(
                                        "Expected at most one active " + label + " but found " + active.size()
                        );
                }

                return active;
        }

        private <T> Optional<T> resolveOptionalActiveInTerminationDate(
                        String label,
                        List<T> activeOccurrences,
                        java.util.function.Function<T, LocalDate> startDateExtractor,
                        LocalDate terminationDate
        ) {
                if (activeOccurrences.isEmpty()) {
                        return Optional.empty();
                }

                T activeOccurrence = activeOccurrences.get(0);
                LocalDate activeStartDate = startDateExtractor.apply(activeOccurrence);

                if (activeStartDate.isAfter(terminationDate)) {
                        return Optional.empty();
                }

                return Optional.of(activeOccurrence);
        }

    private void validateTerminationDateNotBeforeStart(String label, LocalDate startDate, LocalDate terminationDate) {
        if (terminationDate.isBefore(startDate)) {
            throw new TerminateEmployeeConflictException(
                    "terminationDate must be greater than or equal to startDate of " + label
            );
        }
    }

    private String normalizeRequiredText(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new TerminateEmployeeRequestInvalidException(fieldName + " is required");
        }

        return value.trim();
    }

    private String normalizeRequiredCode(String fieldName, String value, boolean allowExtendedLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new TerminateEmployeeRequestInvalidException(fieldName + " is required");
        }

        String normalized = value.trim().toUpperCase();
        if (!allowExtendedLength && normalized.length() > 30) {
            throw new TerminateEmployeeRequestInvalidException(fieldName + " exceeds max length 30");
        }

        return normalized;
    }

    private LocalDate normalizeRequiredDate(LocalDate value) {
        if (value == null) {
            throw new TerminateEmployeeRequestInvalidException("terminationDate is required");
        }

        return value;
    }
}
