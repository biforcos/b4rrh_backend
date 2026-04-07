package com.b4rrhh.employee.workcenter.domain.service;

import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCompanyMismatchException;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterCompanyLookupPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class WorkCenterCompanyValidator {

    private final WorkCenterCompanyLookupPort workCenterCompanyLookupPort;

    public WorkCenterCompanyValidator(WorkCenterCompanyLookupPort workCenterCompanyLookupPort) {
        this.workCenterCompanyLookupPort = workCenterCompanyLookupPort;
    }

    public void validateBelongsToCompany(
            String ruleSystemCode,
            String workCenterCode,
            String companyCode,
            LocalDate referenceDate
    ) {
        String actualCompanyCode = workCenterCompanyLookupPort
                .findCompanyCode(ruleSystemCode, workCenterCode, referenceDate)
                .orElseThrow(() -> new WorkCenterCatalogValueInvalidException("workCenterCode", workCenterCode));

        if (!actualCompanyCode.equals(companyCode)) {
            throw new WorkCenterCompanyMismatchException(workCenterCode, companyCode);
        }
    }
}