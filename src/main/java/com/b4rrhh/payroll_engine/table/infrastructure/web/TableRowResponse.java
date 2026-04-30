package com.b4rrhh.payroll_engine.table.infrastructure.web;

import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TableRowResponse(
        Long id,
        String searchCode,
        LocalDate startDate,
        LocalDate endDate,
        BigDecimal monthlyValue,
        BigDecimal annualValue,
        BigDecimal dailyValue,
        BigDecimal hourlyValue,
        boolean active
) {
    static TableRowResponse from(PayrollTableRow row) {
        return new TableRowResponse(
                row.getId(), row.getSearchCode(), row.getStartDate(), row.getEndDate(),
                row.getMonthlyValue(), row.getAnnualValue(), row.getDailyValue(), row.getHourlyValue(),
                row.isActive()
        );
    }
}
