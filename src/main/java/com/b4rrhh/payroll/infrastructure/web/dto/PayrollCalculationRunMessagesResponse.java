package com.b4rrhh.payroll.infrastructure.web.dto;

import java.util.List;

public record PayrollCalculationRunMessagesResponse(
        Long runId,
        List<PayrollCalculationRunMessageResponse> items
) {
}