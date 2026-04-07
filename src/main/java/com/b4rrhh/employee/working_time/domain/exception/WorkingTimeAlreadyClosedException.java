package com.b4rrhh.employee.working_time.domain.exception;

public class WorkingTimeAlreadyClosedException extends RuntimeException {

    public WorkingTimeAlreadyClosedException(Integer workingTimeNumber) {
        super("Working time already closed for workingTimeNumber=" + workingTimeNumber);
    }
}