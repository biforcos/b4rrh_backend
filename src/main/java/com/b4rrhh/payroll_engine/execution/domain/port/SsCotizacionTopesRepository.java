package com.b4rrhh.payroll_engine.execution.domain.port;

import com.b4rrhh.payroll_engine.execution.domain.model.SsCotizacionTope;

import java.time.LocalDate;
import java.util.Optional;

public interface SsCotizacionTopesRepository {
    Optional<SsCotizacionTope> findActive(String ruleSystemCode, String grupoCode, String periodType, LocalDate referenceDate);
}
