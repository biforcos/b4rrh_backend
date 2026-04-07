package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.domain.model.WorkingTime;

import java.util.List;

public interface ListEmployeeWorkingTimesUseCase {

    List<WorkingTime> listByEmployeeBusinessKey(ListEmployeeWorkingTimesCommand command);
}