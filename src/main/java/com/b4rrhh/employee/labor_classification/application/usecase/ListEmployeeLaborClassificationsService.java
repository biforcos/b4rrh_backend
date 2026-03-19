package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationContext;
import com.b4rrhh.employee.labor_classification.application.port.EmployeeLaborClassificationLookupPort;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationEmployeeNotFoundException;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.domain.port.LaborClassificationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListEmployeeLaborClassificationsService implements ListEmployeeLaborClassificationsUseCase {

    private final LaborClassificationRepository laborClassificationRepository;
    private final EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort;

    public ListEmployeeLaborClassificationsService(
            LaborClassificationRepository laborClassificationRepository,
            EmployeeLaborClassificationLookupPort employeeLaborClassificationLookupPort
    ) {
        this.laborClassificationRepository = laborClassificationRepository;
        this.employeeLaborClassificationLookupPort = employeeLaborClassificationLookupPort;
    }

    @Override
    public List<LaborClassification> listByEmployeeBusinessKey(ListEmployeeLaborClassificationsCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());

        EmployeeLaborClassificationContext employee = employeeLaborClassificationLookupPort
                .findByBusinessKey(normalizedRuleSystemCode, normalizedEmployeeTypeCode, normalizedEmployeeNumber)
                .orElseThrow(() -> new LaborClassificationEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        return laborClassificationRepository.findByEmployeeIdOrderByStartDate(employee.employeeId());
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
