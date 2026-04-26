package com.b4rrhh.payroll_engine.execution.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TechnicalConceptSegmentData(
        LocalDate periodStart,
        LocalDate periodEnd,
        LocalDate segmentStart,
        LocalDate segmentEnd,
        long daysInSegment,
        BigDecimal workingTimePercentage
) {}
