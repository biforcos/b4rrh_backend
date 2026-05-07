package com.b4rrhh.employee.tax_information.infrastructure.web.dto;

import java.util.Map;

public record EmployeeTaxInformationErrorResponse(String code, String message, Map<String, Object> details) {}
