package com.b4rrhh.employee.workcenter.domain.service;

import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.port.EmployeeActiveCompanyLookupPort;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterCompanyLookupPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkCenterEmployeeCompanyDomainService {

    private final EmployeeActiveCompanyLookupPort employeeActiveCompanyLookupPort;
    private final WorkCenterCompanyValidator workCenterCompanyValidator;

    public WorkCenterEmployeeCompanyDomainService(
            EmployeeActiveCompanyLookupPort employeeActiveCompanyLookupPort,
            WorkCenterCompanyLookupPort workCenterCompanyLookupPort
    ) {
        this.employeeActiveCompanyLookupPort = employeeActiveCompanyLookupPort;
        this.workCenterCompanyValidator = new WorkCenterCompanyValidator(workCenterCompanyLookupPort);
    }

    public void validateWorkCenterBelongsToEmployeeCompany(
            Long employeeId,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String workCenterCode,
            LocalDate referenceDate
    ) {
        String employeeCompanyCode = employeeActiveCompanyLookupPort
                .findActiveCompanyCode(employeeId, referenceDate)
                .orElseThrow(() -> new WorkCenterOutsidePresencePeriodException(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        referenceDate,
                        null
                ));

        workCenterCompanyValidator.validateBelongsToCompany(
                ruleSystemCode,
                workCenterCode,
                employeeCompanyCode,
                referenceDate
        );
    }
}