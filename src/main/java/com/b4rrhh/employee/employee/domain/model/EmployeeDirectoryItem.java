package com.b4rrhh.employee.employee.domain.model;

public class EmployeeDirectoryItem {

    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final String displayName;
    private final String status;
    private final String workCenterCode;

    public EmployeeDirectoryItem(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String displayName,
            String status,
            String workCenterCode
    ) {
        this.ruleSystemCode = ruleSystemCode;
        this.employeeTypeCode = employeeTypeCode;
        this.employeeNumber = employeeNumber;
        this.displayName = displayName;
        this.status = status;
        this.workCenterCode = workCenterCode;
    }

    public String getRuleSystemCode() {
        return ruleSystemCode;
    }

    public String getEmployeeTypeCode() {
        return employeeTypeCode;
    }

    public String getEmployeeNumber() {
        return employeeNumber;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getStatus() {
        return status;
    }

    public String getWorkCenterCode() {
        return workCenterCode;
    }
}