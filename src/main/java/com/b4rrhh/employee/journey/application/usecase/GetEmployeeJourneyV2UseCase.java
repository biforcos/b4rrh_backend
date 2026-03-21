package com.b4rrhh.employee.journey.application.usecase;

public interface GetEmployeeJourneyV2UseCase {

    EmployeeJourneyTimelineView get(GetEmployeeJourneyV2Command command);
}