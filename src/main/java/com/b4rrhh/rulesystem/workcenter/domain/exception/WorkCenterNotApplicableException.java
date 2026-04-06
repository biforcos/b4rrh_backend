package com.b4rrhh.rulesystem.workcenter.domain.exception;

import java.time.LocalDate;

public class WorkCenterNotApplicableException extends RuntimeException {

    public WorkCenterNotApplicableException(String ruleSystemCode, String workCenterCode, LocalDate referenceDate) {
        super("Work center exists but is not applicable for business key "
                + ruleSystemCode + "/" + workCenterCode + " at " + referenceDate);
    }
}