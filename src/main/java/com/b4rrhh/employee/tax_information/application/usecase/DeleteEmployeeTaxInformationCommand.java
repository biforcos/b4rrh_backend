package com.b4rrhh.employee.tax_information.application.usecase;

import java.time.LocalDate;

public record DeleteEmployeeTaxInformationCommand(
    String ruleSystemCode, String employeeTypeCode, String employeeNumber, LocalDate validFrom) {}
