package com.b4rrhh.rulesystem.employeenumbering.domain.exception;

public class EmployeeNumberingConfigNotFoundException extends RuntimeException {
    public EmployeeNumberingConfigNotFoundException(String ruleSystemCode) {
        super("No employee numbering config for rule system: " + ruleSystemCode);
    }
}
