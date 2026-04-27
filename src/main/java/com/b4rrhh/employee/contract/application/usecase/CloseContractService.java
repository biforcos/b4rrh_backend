package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.application.service.ContractPresenceCoverageValidator;
import com.b4rrhh.employee.contract.domain.exception.ContractAlreadyClosedException;
import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractNotFoundException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.domain.port.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class CloseContractService implements CloseContractUseCase {

    private final ContractRepository contractRepository;
    private final EmployeeContractLookupPort employeeContractLookupPort;
    private final ContractPresenceCoverageValidator contractPresenceCoverageValidator;

    public CloseContractService(
            ContractRepository contractRepository,
            EmployeeContractLookupPort employeeContractLookupPort,
            ContractPresenceCoverageValidator contractPresenceCoverageValidator
    ) {
        this.contractRepository = contractRepository;
        this.employeeContractLookupPort = employeeContractLookupPort;
        this.contractPresenceCoverageValidator = contractPresenceCoverageValidator;
    }

    @Override
    @Transactional
    public Contract close(CloseContractCommand command) {
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

        Contract closed = existing.close(command.endDate());

        contractPresenceCoverageValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                closed.getStartDate(),
                closed.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<Contract> projectedHistory = replaceByStartDate(
                contractRepository.findByEmployeeIdOrderByStartDate(employee.employeeId()),
                closed
        );

        contractPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        contractRepository.update(closed, closed.getStartDate());
        return closed;
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
