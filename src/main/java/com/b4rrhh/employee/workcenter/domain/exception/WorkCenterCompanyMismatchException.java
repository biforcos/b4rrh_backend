package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterCompanyMismatchException extends RuntimeException {

    public WorkCenterCompanyMismatchException(String workCenterCode, String companyCode) {
        super("Work center " + workCenterCode + " does not belong to company " + companyCode);
    }
}