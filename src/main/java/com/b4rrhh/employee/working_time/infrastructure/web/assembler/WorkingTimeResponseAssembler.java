package com.b4rrhh.employee.working_time.infrastructure.web.assembler;

import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.infrastructure.web.dto.WorkingTimeResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WorkingTimeResponseAssembler {

    public WorkingTimeResponse toResponse(WorkingTime workingTime) {
        return new WorkingTimeResponse(
                workingTime.getWorkingTimeNumber(),
                workingTime.getStartDate(),
                workingTime.getEndDate(),
                workingTime.getWorkingTimePercentage(),
                workingTime.getWeeklyHours(),
                workingTime.getDailyHours(),
                workingTime.getMonthlyHours()
        );
    }

    public List<WorkingTimeResponse> toResponseList(List<WorkingTime> workingTimes) {
        return workingTimes.stream()
                .map(this::toResponse)
                .toList();
    }
}