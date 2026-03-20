package com.b4rrhh.employee.journey.application.usecase;

public interface GetEmployeeJourneyUseCase {

    EmployeeJourneyView get(GetEmployeeJourneyCommand command);
}
