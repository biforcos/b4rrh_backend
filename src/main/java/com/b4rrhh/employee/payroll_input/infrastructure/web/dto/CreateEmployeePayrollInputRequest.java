package com.b4rrhh.employee.payroll_input.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.math.BigDecimal;

public class CreateEmployeePayrollInputRequest {

    private String conceptCode;
    private int period;
    private BigDecimal quantity;

    public String getConceptCode() { return conceptCode; }
    public void setConceptCode(String conceptCode) { this.conceptCode = conceptCode; }
    public int getPeriod() { return period; }
    public void setPeriod(int period) { this.period = period; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("Unexpected field: " + fieldName);
    }
}
