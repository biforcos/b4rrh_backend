package com.b4rrhh.employee.labor_classification.infrastructure.rest.dto;

import java.util.Map;

public record LaborClassificationErrorResponse(
	String code,
	String message,
	Map<String, Object> details
) {
}
