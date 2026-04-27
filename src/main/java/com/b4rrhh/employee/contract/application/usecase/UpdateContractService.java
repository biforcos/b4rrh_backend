package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.UpdateContractCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.application.service.ContractSubtypeRelationValidator;
import com.b4rrhh.employee.contract.application.service.ContractCatalogValidator;
import com.b4rrhh.employee.contract.application.service.ContractPresenceCoverageValidator;
import com.b4rrhh.employee.contract.domain.exception.ContractAlreadyClosedException;
import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractOverlapException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.domain.port.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class UpdateContractService implements UpdateContractUseCase {

    private final ContractRepository contractRepository;
    private final EmployeeContractLookupPort employeeContractLookupPort;
    private final ContractCatalogValidator contractCatalogValidator;
    private final ContractSubtypeRelationValidator contractSubtypeRelationValidator;
    private final ContractPresenceCoverageValidator contractPresenceCoverageValidator;

    public UpdateContractService(
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
    public Contract update(UpdateContractCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        LocalDate normalizedStartDate = normalizeStartDate(command.startDate());

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

        Contract existing = contractRepository
                .findByEmployeeIdAndStartDate(employee.employeeId(), normalizedStartDate)
                .orElseThrow(() -> new ContractNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedStartDate
                ));

        if (!existing.isActive()) {
            throw new ContractAlreadyClosedException(existing.getStartDate());
        }

        String normalizedContractCode = contractCatalogValidator
                .normalizeRequiredCode("contractCode", command.contractCode());
        String normalizedContractSubtypeCode = contractCatalogValidator
                .normalizeRequiredCode("contractSubtypeCode", command.contractSubtypeCode());

        contractCatalogValidator.validateContractCode(
                normalizedRuleSystemCode,
                normalizedContractCode,
                existing.getStartDate()
        );
        contractCatalogValidator.validateContractSubtypeCode(
                normalizedRuleSystemCode,
                normalizedContractSubtypeCode,
                existing.getStartDate()
        );
        contractSubtypeRelationValidator.validateContractSubtypeRelation(
                normalizedRuleSystemCode,
                normalizedContractCode,
                normalizedContractSubtypeCode,
                existing.getStartDate()
        );

        LocalDate effectiveStartDate = (command.newStartDate() != null)
                ? command.newStartDate()
                : normalizedStartDate;

        Contract updated = existing
                .correctStartDate(effectiveStartDate)
                .updateContract(normalizedContractCode, normalizedContractSubtypeCode);

        List<Contract> fullHistory = contractRepository
                .findByEmployeeIdOrderByStartDate(employee.employeeId());

        Contract cascadedPredecessor = null;
        if (!effectiveStartDate.equals(normalizedStartDate)) {
            LocalDate expectedPredecessorEnd = normalizedStartDate.minusDays(1);
            Contract predecessor = fullHistory.stream()
                    .filter(c -> expectedPredecessorEnd.equals(c.getEndDate()))
                    .findFirst()
                    .orElse(null);
            if (predecessor != null) {
                cascadedPredecessor = predecessor.adjustEndDate(effectiveStartDate.minusDays(1));
                contractRepository.update(cascadedPredecessor, cascadedPredecessor.getStartDate());
            }
        }

        if (contractRepository.existsOverlappingPeriod(
                employee.employeeId(),
                updated.getStartDate(),
                updated.getEndDate(),
                normalizedStartDate
        )) {
            throw new ContractOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    updated.getStartDate(),
                    updated.getEndDate()
            );
        }

        contractPresenceCoverageValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                updated.getStartDate(),
                updated.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<Contract> projectedHistory = buildProjectedHistory(
                fullHistory,
                cascadedPredecessor,
                updated,
                normalizedStartDate
        );

        contractPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        contractRepository.update(updated, normalizedStartDate);
        return updated;
    }

    private List<Contract> buildProjectedHistory(
            List<Contract> history,
            Contract cascadedPredecessor,
            Contract updated,
            LocalDate oldStartDate
    ) {
        List<Contract> projected = new ArrayList<>(history.size());
        for (Contract contract : history) {
            if (contract.getStartDate().equals(oldStartDate)) {
                projected.add(updated);
            } else if (cascadedPredecessor != null
                    && contract.getStartDate().equals(cascadedPredecessor.getStartDate())) {
                projected.add(cascadedPredecessor);
            } else {
                projected.add(contract);
            }
        }
        return projected;
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

    private LocalDate normalizeStartDate(LocalDate startDate) {
        if (startDate == null) {
            throw new IllegalArgumentException("startDate is required");
        }

        return startDate;
    }
}
