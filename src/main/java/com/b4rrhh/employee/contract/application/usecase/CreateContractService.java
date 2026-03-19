package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
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

import java.util.ArrayList;
import java.util.List;

@Service
public class CreateContractService implements CreateContractUseCase {

    private final ContractRepository contractRepository;
    private final EmployeeContractLookupPort employeeContractLookupPort;
    private final ContractCatalogValidator contractCatalogValidator;
    private final ContractSubtypeRelationValidator contractSubtypeRelationValidator;
    private final ContractPresenceCoverageValidator contractPresenceCoverageValidator;

    public CreateContractService(
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
    public Contract create(CreateContractCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

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
                command.startDate()
        );
        contractCatalogValidator.validateContractSubtypeCode(
                normalizedRuleSystemCode,
                normalizedContractSubtypeCode,
                command.startDate()
        );
        contractSubtypeRelationValidator.validateContractSubtypeRelation(
                normalizedRuleSystemCode,
                normalizedContractCode,
                normalizedContractSubtypeCode,
                command.startDate()
        );

        Contract newContract = new Contract(
                employee.employeeId(),
                normalizedContractCode,
                normalizedContractSubtypeCode,
                command.startDate(),
                command.endDate()
        );

        if (contractRepository.existsOverlappingPeriod(
                employee.employeeId(),
                newContract.getStartDate(),
                newContract.getEndDate(),
                null
        )) {
            throw new ContractOverlapException(
                    normalizedRuleSystemCode,
                    normalizedEmployeeTypeCode,
                    normalizedEmployeeNumber,
                    newContract.getStartDate(),
                    newContract.getEndDate()
            );
        }

        contractPresenceCoverageValidator.validatePeriodWithinPresence(
                employee.employeeId(),
                newContract.getStartDate(),
                newContract.getEndDate(),
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        List<Contract> projectedHistory = new ArrayList<>(
                contractRepository.findByEmployeeIdOrderByStartDate(employee.employeeId())
        );
        projectedHistory.add(newContract);

        contractPresenceCoverageValidator.validateFullCoverage(
                employee.employeeId(),
                projectedHistory,
                normalizedRuleSystemCode,
                normalizedEmployeeTypeCode,
                normalizedEmployeeNumber
        );

        contractRepository.save(newContract);
        return newContract;
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
}
