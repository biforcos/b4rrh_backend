package com.b4rrhh.employee.working_time.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;

import java.time.LocalDate;

public class CloseWorkingTimeRequest {

    private LocalDate endDate;

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("Unexpected field: " + fieldName);
    }
}