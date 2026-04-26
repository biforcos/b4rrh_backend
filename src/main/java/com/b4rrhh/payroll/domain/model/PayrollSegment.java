package com.b4rrhh.payroll.domain.model;

import java.time.LocalDate;

public record PayrollSegment(LocalDate segmentStart) {

    public PayrollSegment {
        if (segmentStart == null) {
            throw new IllegalArgumentException("segmentStart is required");
        }
    }
}
