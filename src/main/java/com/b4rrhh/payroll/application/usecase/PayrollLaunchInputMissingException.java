package com.b4rrhh.payroll.application.usecase;

import java.util.Map;

public class PayrollLaunchInputMissingException extends RuntimeException {

    private final String reasonCode;
    private final Map<String, Object> details;

    public PayrollLaunchInputMissingException(String reasonCode, String message, Map<String, Object> details) {
        super(message);
        this.reasonCode = reasonCode;
        this.details = details == null ? Map.of() : Map.copyOf(details);
    }

    public String getReasonCode() {
        return reasonCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
