package com.b4rrhh.employee.workcenter.domain.exception;

public class WorkCenterAlreadyClosedException extends RuntimeException {

    public WorkCenterAlreadyClosedException(Integer workCenterAssignmentNumber) {
        super("Work center assignment is already closed: " + workCenterAssignmentNumber);
    }
}