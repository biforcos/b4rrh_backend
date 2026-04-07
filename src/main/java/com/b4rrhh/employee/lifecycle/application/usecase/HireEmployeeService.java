package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeDefaultValues;
import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.cost_center.application.usecase.CostCenterDistributionItem;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterDistributionCommand;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterDistributionUseCase;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterCatalogValueInvalidException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterDistributionInvalidException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterDistributionWindow;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeCommand;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCategoryInvalidException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeAlreadyExistsException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeDependentRelationInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeRequestInvalidException;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.domain.service.WorkCenterCompanyValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
public class HireEmployeeService implements HireEmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final CreatePresenceUseCase createPresenceUseCase;
    private final CreateLaborClassificationUseCase createLaborClassificationUseCase;
    private final CreateContractUseCase createContractUseCase;
    private final CreateWorkCenterUseCase createWorkCenterUseCase;
    private final CreateCostCenterDistributionUseCase createCostCenterDistributionUseCase;
    private final WorkCenterCompanyValidator workCenterCompanyValidator;

    public HireEmployeeService(
            EmployeeRepository employeeRepository,
            CreateEmployeeUseCase createEmployeeUseCase,
            CreatePresenceUseCase createPresenceUseCase,
            CreateLaborClassificationUseCase createLaborClassificationUseCase,
            CreateContractUseCase createContractUseCase,
            CreateWorkCenterUseCase createWorkCenterUseCase,
            CreateCostCenterDistributionUseCase createCostCenterDistributionUseCase,
            WorkCenterCompanyValidator workCenterCompanyValidator
    ) {
        this.employeeRepository = employeeRepository;
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.createPresenceUseCase = createPresenceUseCase;
        this.createLaborClassificationUseCase = createLaborClassificationUseCase;
        this.createContractUseCase = createContractUseCase;
        this.createWorkCenterUseCase = createWorkCenterUseCase;
        this.createCostCenterDistributionUseCase = createCostCenterDistributionUseCase;
        this.workCenterCompanyValidator = workCenterCompanyValidator;
    }

    @Override
    @Transactional
    public HireEmployeeResult hire(HireEmployeeCommand command) {
        if (command == null) {
            throw new HireEmployeeRequestInvalidException("request body is required");
        }

        String ruleSystemCode = normalizeRequiredCode("ruleSystemCode", command.ruleSystemCode(), false);
        String employeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String employeeNumber = normalizeRequiredText("employeeNumber", command.employeeNumber());
        String firstName = normalizeRequiredText("firstName", command.firstName());
        String lastName1 = normalizeRequiredText("lastName1", command.lastName1());
        String lastName2 = normalizeOptionalText(command.lastName2());
        String preferredName = normalizeOptionalText(command.preferredName());
        LocalDate hireDate = normalizeRequiredDate(command.hireDate());

        String companyCode = normalizeRequiredCode("companyCode", command.companyCode(), false);
        String entryReasonCode = normalizeRequiredCode("entryReasonCode", command.entryReasonCode(), false);
        String workCenterCode = normalizeRequiredCode("workCenterCode", command.workCenterCode(), false);

        if (employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber(
                ruleSystemCode, employeeTypeCode, employeeNumber).isPresent()) {
            throw new HireEmployeeAlreadyExistsException(ruleSystemCode, employeeTypeCode, employeeNumber);
        }

        workCenterCompanyValidator.validateBelongsToCompany(
            ruleSystemCode,
            workCenterCode,
            companyCode,
            hireDate
        );

        Employee createdEmployee = createEmployeeUseCase.create(new CreateEmployeeCommand(
                ruleSystemCode, employeeTypeCode, employeeNumber, firstName, lastName1, lastName2, preferredName
        ));

        Presence createdPresence;
        LaborClassification createdLaborClassification;
        Contract createdContract;
        WorkCenter createdWorkCenter;
        CostCenterDistributionWindow createdCostCenter = null;

        try {
            createdPresence = createPresenceUseCase.create(new CreatePresenceCommand(
                    ruleSystemCode, employeeTypeCode, employeeNumber, companyCode, entryReasonCode, null, hireDate, null
            ));

            createdLaborClassification = createLaborClassificationUseCase.create(new CreateLaborClassificationCommand(
                    ruleSystemCode, employeeTypeCode, employeeNumber,
                    command.laborClassification().agreementCode(),
                    command.laborClassification().agreementCategoryCode(),
                    hireDate, null
            ));

            createdContract = createContractUseCase.create(new CreateContractCommand(
                    ruleSystemCode, employeeTypeCode, employeeNumber,
                    command.contract().contractTypeCode(),
                    command.contract().contractSubtypeCode(),
                    hireDate, null
            ));

            createdWorkCenter = createWorkCenterUseCase.create(new CreateWorkCenterCommand(
                    ruleSystemCode, employeeTypeCode, employeeNumber, workCenterCode, hireDate, null
            ));

            if (command.costCenterDistribution() != null) {
                createdCostCenter = createCostCenterDistributionUseCase.create(new CreateCostCenterDistributionCommand(
                        ruleSystemCode, employeeTypeCode, employeeNumber, hireDate,
                        command.costCenterDistribution().items().stream()
                                .map(item -> new CostCenterDistributionItem(item.costCenterCode(), BigDecimal.valueOf(item.allocationPercentage())))
                                .collect(Collectors.toList())
                ));
            }
        } catch (PresenceCatalogValueInvalidException
                 | LaborClassificationAgreementInvalidException
                 | LaborClassificationCategoryInvalidException
                 | ContractInvalidException
                 | ContractSubtypeInvalidException
                 | WorkCenterCatalogValueInvalidException
                 | CostCenterCatalogValueInvalidException
                 | CostCenterDistributionInvalidException ex) {
            throw new HireEmployeeCatalogValueInvalidException(ex.getMessage(), ex);
        } catch (LaborClassificationAgreementCategoryRelationInvalidException
                 | ContractSubtypeRelationInvalidException ex) {
            throw new HireEmployeeDependentRelationInvalidException(ex.getMessage(), ex);
        }

        return new HireEmployeeResult(
                new HireEmployeeResult.EmployeeSummary(
                        createdEmployee.getRuleSystemCode(),
                        createdEmployee.getEmployeeTypeCode(),
                        createdEmployee.getEmployeeNumber(),
                        createdEmployee.getFirstName(),
                        createdEmployee.getLastName1(),
                        createdEmployee.getLastName2(),
                        createdEmployee.getPreferredName(),
                        formatDisplayName(createdEmployee),
                        createdEmployee.getStatus(),
                        hireDate
                ),
                new HireEmployeeResult.PresenceSummary(
                        createdPresence.getPresenceNumber(),
                        createdPresence.getStartDate(),
                        createdPresence.getCompanyCode(),
                        createdPresence.getEntryReasonCode()
                ),
                new HireEmployeeResult.WorkCenterSummary(
                        createdWorkCenter.getStartDate(),
                        createdWorkCenter.getWorkCenterCode(),
                        createdWorkCenter.getWorkCenterCode() // name not available in domain model, use code
                ),
                createdCostCenter != null ? new HireEmployeeResult.CostCenterSummary(
                        createdCostCenter.getStartDate(),
                        createdCostCenter.getTotalAllocationPercentage().doubleValue(),
                        createdCostCenter.getItems().stream()
                                .map(item -> new HireEmployeeResult.CostCenterItemSummary(
                                        item.getCostCenterCode(),
                                        item.getCostCenterCode(), // name not available, use code
                                        item.getAllocationPercentage().doubleValue()
                                ))
                                .collect(Collectors.toList())
                ) : null,
                new HireEmployeeResult.ContractSummary(
                        createdContract.getStartDate(),
                        createdContract.getContractCode(),
                        createdContract.getContractSubtypeCode()
                ),
                new HireEmployeeResult.LaborClassificationSummary(
                        createdLaborClassification.getStartDate(),
                        createdLaborClassification.getAgreementCode(),
                        createdLaborClassification.getAgreementCategoryCode()
                )
        );
    }

    private String formatDisplayName(Employee employee) {
        StringBuilder sb = new StringBuilder();
        sb.append(employee.getFirstName());
        sb.append(" ");
        sb.append(employee.getLastName1());
        if (employee.getLastName2() != null && !employee.getLastName2().isEmpty()) {
            sb.append(" ");
            sb.append(employee.getLastName2());
        }
        return sb.toString();
    }

    private String normalizeRequiredText(String fieldName, String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new HireEmployeeRequestInvalidException(fieldName + " is required");
        }
        return value.trim();
    }

    private String normalizeRequiredCode(String fieldName, String value, boolean allowTrimOnly) {
        if (value == null || value.trim().isEmpty()) {
            throw new HireEmployeeRequestInvalidException(fieldName + " is required");
        }
        String normalized = value.trim().toUpperCase();
        if (!allowTrimOnly && normalized.isEmpty()) {
            throw new HireEmployeeRequestInvalidException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeEmployeeTypeCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            return HireEmployeeDefaultValues.DEFAULT_EMPLOYEE_TYPE_CODE;
        }
        return value.trim().toUpperCase();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    private LocalDate normalizeRequiredDate(LocalDate value) {
        if (value == null) {
            throw new HireEmployeeRequestInvalidException("hireDate is required");
        }
        return value;
    }
}
