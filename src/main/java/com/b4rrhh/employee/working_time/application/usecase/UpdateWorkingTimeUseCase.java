package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.domain.model.WorkingTime;

public interface UpdateWorkingTimeUseCase {
    WorkingTime update(UpdateWorkingTimeCommand command);
}
