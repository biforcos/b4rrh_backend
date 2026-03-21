package com.b4rrhh.employee.journey.application.usecase;

public enum JourneyEventStatus {
    COMPLETED("completed"),
    CURRENT("current"),
    FUTURE("future");

    private final String apiValue;

    JourneyEventStatus(String apiValue) {
        this.apiValue = apiValue;
    }

    public String apiValue() {
        return apiValue;
    }
}