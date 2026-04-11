package com.b4rrhh.payroll.domain.model;

public enum PayrollStatus {
    NOT_VALID,
    CALCULATED,
    EXPLICIT_VALIDATED,
    DEFINITIVE;

    public boolean canBeRecalculated() {
        return this == NOT_VALID;
    }
}