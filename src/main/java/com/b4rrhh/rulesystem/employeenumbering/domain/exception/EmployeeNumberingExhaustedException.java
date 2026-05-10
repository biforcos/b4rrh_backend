package com.b4rrhh.rulesystem.employeenumbering.domain.exception;

public class EmployeeNumberingExhaustedException extends RuntimeException {
    public EmployeeNumberingExhaustedException(String ruleSystemCode) {
        super("Employee numbering counter exhausted for rule system: " + ruleSystemCode);
    }
}
