package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.math.BigDecimal;

public class UpdateEmployeePayrollInputRequest {

    private BigDecimal quantity;

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("Unexpected field: " + fieldName);
    }
}
