package com.b4rrhh.payroll_engine.execution.domain.exception;

/**
 * Thrown when the technical value resolver encounters a concept code
 * that is not supported in this PoC iteration.
 */
public class UnsupportedTechnicalConceptException extends RuntimeException {

    public UnsupportedTechnicalConceptException(String conceptCode) {
        super("Technical concept not supported in PoC resolver: '" + conceptCode +
              "'. Supported codes: T_DIAS_PRESENCIA_SEGMENTO, T_SALARIO_MENSUAL, T_FACTOR_JORNADA, T_PRECIO_DIA.");
    }
}
