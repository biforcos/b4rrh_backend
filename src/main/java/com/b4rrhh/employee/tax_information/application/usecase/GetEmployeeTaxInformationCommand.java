package com.b4rrhh.employee.tax_information.application.usecase;

import java.time.LocalDate;

public record GetEmployeeTaxInformationCommand(
    String ruleSystemCode, String employeeTypeCode, String employeeNumber, LocalDate validFrom) {}
