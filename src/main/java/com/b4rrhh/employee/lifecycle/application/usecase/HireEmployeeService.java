package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeRelationInvalidException;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeCommand;
import com.b4rrhh.employee.employee.application.usecase.CreateEmployeeUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.domain.port.EmployeeRepository;
import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCategoryInvalidException;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class HireEmployeeService implements HireEmployeeUseCase {

    private final EmployeeRepository employeeRepository;
    private final CreateEmployeeUseCase createEmployeeUseCase;
    private final CreatePresenceUseCase createPresenceUseCase;
    private final CreateLaborClassificationUseCase createLaborClassificationUseCase;
    private final CreateContractUseCase createContractUseCase;
    private final CreateWorkCenterUseCase createWorkCenterUseCase;

    public HireEmployeeService(
            EmployeeRepository employeeRepository,
            CreateEmployeeUseCase createEmployeeUseCase,
            CreatePresenceUseCase createPresenceUseCase,
            CreateLaborClassificationUseCase createLaborClassificationUseCase,
            CreateContractUseCase createContractUseCase,
            CreateWorkCenterUseCase createWorkCenterUseCase
    ) {
        this.employeeRepository = employeeRepository;
        this.createEmployeeUseCase = createEmployeeUseCase;
        this.createPresenceUseCase = createPresenceUseCase;
        this.createLaborClassificationUseCase = createLaborClassificationUseCase;
        this.createContractUseCase = createContractUseCase;
        this.createWorkCenterUseCase = createWorkCenterUseCase;
    }

    @Override
    @Transactional
    public HireEmployeeResult hire(HireEmployeeCommand command) {
        if (command == null) {
            throw new HireEmployeeRequestInvalidException("request body is required");
        }

        String ruleSystemCode = normalizeRequiredCode("ruleSystemCode", command.ruleSystemCode(), false);
        String employeeTypeCode = normalizeRequiredCode("employeeTypeCode", command.employeeTypeCode(), false);
        String employeeNumber = normalizeRequiredText("employeeNumber", command.employeeNumber());
        String firstName = normalizeRequiredText("firstName", command.firstName());
        String lastName1 = normalizeRequiredText("lastName1", command.lastName1());
        String lastName2 = normalizeOptionalText(command.lastName2());
        String preferredName = normalizeOptionalText(command.preferredName());
        LocalDate hireDate = normalizeRequiredDate(command.hireDate());

        String companyCode = normalizeRequiredCode("companyCode", command.companyCode(), false);
        String entryReasonCode = normalizeRequiredCode("entryReasonCode", command.entryReasonCode(), false);
        String agreementCode = normalizeRequiredCode("agreementCode", command.agreementCode(), false);
        String agreementCategoryCode = normalizeRequiredCode("agreementCategoryCode", command.agreementCategoryCode(), false);
        String contractTypeCode = normalizeRequiredCode("contractTypeCode", command.contractTypeCode(), false);
        String contractSubtypeCode = normalizeRequiredCode("contractSubtypeCode", command.contractSubtypeCode(), false);
        String workCenterCode = normalizeRequiredCode("workCenterCode", command.workCenterCode(), false);

        employeeRepository.findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumber(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber
        ).ifPresent(existing -> {
            throw new HireEmployeeAlreadyExistsException(ruleSystemCode, employeeTypeCode, employeeNumber);
        });

        Employee createdEmployee = createEmployeeUseCase.create(new CreateEmployeeCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                firstName,
                lastName1,
                lastName2,
                preferredName
        ));

        Presence createdPresence;
        WorkCenter createdWorkCenter;

        try {
            createdPresence = createPresenceUseCase.create(new CreatePresenceCommand(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    companyCode,
                    entryReasonCode,
                    null,
                    hireDate,
                    null
            ));

            createLaborClassificationUseCase.create(new CreateLaborClassificationCommand(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    agreementCode,
                    agreementCategoryCode,
                    hireDate,
                    null
            ));

            createContractUseCase.create(new CreateContractCommand(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    contractTypeCode,
                    contractSubtypeCode,
                    hireDate,
                    null
            ));

                createdWorkCenter = createWorkCenterUseCase.create(new CreateWorkCenterCommand(
                    ruleSystemCode,
                    employeeTypeCode,
                    employeeNumber,
                    workCenterCode,
                    hireDate,
                    null
            ));
        } catch (PresenceCatalogValueInvalidException
                 | LaborClassificationAgreementInvalidException
                 | LaborClassificationCategoryInvalidException
                 | ContractInvalidException
                 | ContractSubtypeInvalidException
                 | WorkCenterCatalogValueInvalidException ex) {
            throw new HireEmployeeCatalogValueInvalidException(ex.getMessage(), ex);
        } catch (LaborClassificationAgreementCategoryRelationInvalidException
                 | ContractSubtypeRelationInvalidException ex) {
            throw new HireEmployeeDependentRelationInvalidException(ex.getMessage(), ex);
        }

        return new HireEmployeeResult(
                createdEmployee.getRuleSystemCode(),
                createdEmployee.getEmployeeTypeCode(),
                createdEmployee.getEmployeeNumber(),
                createdEmployee.getFirstName(),
                createdEmployee.getLastName1(),
                createdEmployee.getLastName2(),
                createdEmployee.getPreferredName(),
                createdEmployee.getStatus(),
                hireDate,
                createdPresence.getPresenceNumber(),
                companyCode,
                entryReasonCode,
                agreementCode,
                agreementCategoryCode,
                contractTypeCode,
                contractSubtypeCode,
                createdWorkCenter.getWorkCenterAssignmentNumber(),
                workCenterCode
        );
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
