package com.b4rrhh.employee.workcenter.infrastructure.web.dto;

import java.time.LocalDate;

public record CloseWorkCenterRequest(LocalDate endDate) {
}