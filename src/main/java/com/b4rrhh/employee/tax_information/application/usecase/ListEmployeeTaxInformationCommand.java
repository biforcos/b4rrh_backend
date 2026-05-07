package com.b4rrhh.employee.tax_information.application.usecase;

public record ListEmployeeTaxInformationCommand(
    String ruleSystemCode, String employeeTypeCode, String employeeNumber) {}
