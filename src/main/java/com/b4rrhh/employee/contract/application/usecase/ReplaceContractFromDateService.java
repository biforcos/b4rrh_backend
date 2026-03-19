package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ReplaceContractFromDateCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.application.service.ContractSubtypeRelationValidator;
import com.b4rrhh.employee.contract.application.service.ContractCatalogValidator;
import com.b4rrhh.employee.contract.application.service.ContractPresenceCoverageValidator;
import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractOverlapException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.domain.port.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ReplaceContractFromDateService implements ReplaceContractFromDateUseCase {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final ContractRepository contractRepository;
    private final EmployeeContractLookupPort employeeContractLookupPort;
    private final ContractCatalogValidator contractCatalogValidator;
    private final ContractSubtypeRelationValidator contractSubtypeRelationValidator;
    private final ContractPresenceCoverageValidator contractPresenceCoverageValidator;

    public ReplaceContractFromDateService(
            ContractRepository contractRepository,
            EmployeeContractLookupPort employeeContractLookupPort,
            ContractCatalogValidator contractCatalogValidator,
            ContractSubtypeRelationValidator contractSubtypeRelationValidator,
            ContractPresenceCoverageValidator contractPresenceCoverageValidator
    ) {
        this.contractRepository = contractRepository;
        this.employeeContractLookupPort = employeeContractLookupPort;
        this.contractCatalogValidator = contractCatalogValidator;
        this.contractSubtypeRelationValidator = contractSubtypeRelationValidator;
        this.contractPresenceCoverageValidator = contractPresenceCoverageValidator;
    }

    @Override
    @Transactional
    public Contract replaceFromDate(ReplaceContractFromDateCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        LocalDate normalizedEffectiveDate = normalizeEffectiveDate(command.effectiveDate());

        EmployeeContractContext employee = employeeContractLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new ContractEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        String normalizedContractCode = contractCatalogValidator
                .normalizeRequiredCode("contractCode", command.contractCode());
        String normalizedContractSubtypeCode = contractCatalogValidator
                .normalizeRequiredCode("contractSubtypeCode", command.contractSubtypeCode());

        contractCatalogValidator.validateContractCode(
                normalizedRuleSystemCode,
                normalizedContractCode,
                normalizedEffectiveDate
        );
        contractCatalogValidator.validateContractSubtypeCode(
                normalizedRuleSystemCode,
                normalizedContractSubtypeCode,
                normalizedEffectiveDate
        );
        contractSubtypeRelationValidator.validateContractSubtypeRelation(
                normalizedRuleSystemCode,
                normalizedContractCode,
                normalizedContractSubtypeCode,
                normalizedEffectiveDate
        );

        List<Contract> currentHistory = contractRepository
                .findByEmployeeIdOrderByStartDate(employee.employeeId())
                .stream()
                .sorted(Comparator.comparing(Contract::getStartDate))
                .toList();

        ReplacementPlan replacementPlan = buildReplacementPlan(
                employee.employeeId(),
                currentHistory,
                normalizedEffectiveDate,
                normalizedContractCode,
                normalizedContractSubtypeCode
        );

        validateNoOverlap(
                replacementPlan.projectedHistory(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        contractPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                replacementPlan.projectedHistory(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        if (replacementPlan.periodToUpdate() != null) {
            contractRepository.update(replacementPlan.periodToUpdate());
        }
        if (replacementPlan.periodToSave() != null) {
            contractRepository.save(replacementPlan.periodToSave());
        }

        return replacementPlan.resultPeriod();
    }

    private ReplacementPlan buildReplacementPlan(
            Long employeeId,
            List<Contract> currentHistory,
            LocalDate effectiveDate,
            String contractCode,
            String contractSubtypeCode
    ) {
        Contract coveringPeriod = currentHistory.stream()
                .filter(period -> isCoveringDate(period, effectiveDate))
                .findFirst()
                .orElse(null);

        if (coveringPeriod == null) {
            Contract newPeriod = new Contract(
                    employeeId,
                    contractCode,
                    contractSubtypeCode,
                    effectiveDate,
                    null
            );

            List<Contract> projected = new ArrayList<>(currentHistory);
            projected.add(newPeriod);
            projected.sort(Comparator.comparing(Contract::getStartDate));

            return new ReplacementPlan(null, newPeriod, newPeriod, projected);
        }

        if (coveringPeriod.getStartDate().equals(effectiveDate)) {
            Contract replaced = new Contract(
                    employeeId,
                    contractCode,
                    contractSubtypeCode,
                    coveringPeriod.getStartDate(),
                    coveringPeriod.getEndDate()
            );

            List<Contract> projected = replaceByStartDate(currentHistory, replaced);
            return new ReplacementPlan(replaced, null, replaced, projected);
        }

        Contract adjustedExisting = new Contract(
                employeeId,
                coveringPeriod.getContractCode(),
                coveringPeriod.getContractSubtypeCode(),
                coveringPeriod.getStartDate(),
                effectiveDate.minusDays(1)
        );

        Contract replacement = new Contract(
                employeeId,
                contractCode,
                contractSubtypeCode,
                effectiveDate,
                coveringPeriod.getEndDate()
        );

        List<Contract> projected = replaceByStartDate(currentHistory, adjustedExisting);
        projected.add(replacement);
        projected.sort(Comparator.comparing(Contract::getStartDate));

        return new ReplacementPlan(adjustedExisting, replacement, replacement, projected);
    }

    private List<Contract> replaceByStartDate(
            List<Contract> history,
            Contract updated
    ) {
        List<Contract> projected = new ArrayList<>(history.size());
        for (Contract contract : history) {
            if (contract.getStartDate().equals(updated.getStartDate())) {
                projected.add(updated);
            } else {
                projected.add(contract);
            }
        }

        return projected;
    }

    private boolean isCoveringDate(Contract period, LocalDate date) {
        if (period.getStartDate().isAfter(date)) {
            return false;
        }

        return period.getEndDate() == null || !period.getEndDate().isBefore(date);
    }

    private void validateNoOverlap(
            List<Contract> projectedHistory,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        List<Contract> sorted = projectedHistory.stream()
                .sorted(Comparator.comparing(Contract::getStartDate))
                .toList();

        for (int index = 1; index < sorted.size(); index++) {
            Contract previous = sorted.get(index - 1);
            Contract current = sorted.get(index);
            LocalDate previousEnd = normalizeEndDate(previous.getEndDate());

            if (!current.getStartDate().isAfter(previousEnd)) {
                throw new ContractOverlapException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        current.getStartDate(),
                        current.getEndDate()
                );
            }
        }
    }

    private LocalDate normalizeEndDate(LocalDate endDate) {
        return endDate == null ? MAX_DATE : endDate;
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
            Contract periodToUpdate,
            Contract periodToSave,
            Contract resultPeriod,
            List<Contract> projectedHistory
    ) {
    }
}
