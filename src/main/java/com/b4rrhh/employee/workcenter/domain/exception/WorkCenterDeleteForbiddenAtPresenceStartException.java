package com.b4rrhh.employee.workcenter.domain.exception;

import java.time.LocalDate;

public class WorkCenterDeleteForbiddenAtPresenceStartException extends RuntimeException {

    public WorkCenterDeleteForbiddenAtPresenceStartException(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Integer workCenterAssignmentNumber,
            LocalDate startDate
    ) {
        super("La asignación no puede eliminarse porque inicia una presence del empleado. Corrígela si necesitas cambiarla.");
    }
}