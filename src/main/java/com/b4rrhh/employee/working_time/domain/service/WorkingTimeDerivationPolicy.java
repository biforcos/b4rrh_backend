package com.b4rrhh.employee.working_time.domain.service;

import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;

import java.math.BigDecimal;

public interface WorkingTimeDerivationPolicy {

    WorkingTimeDerivedHours derive(BigDecimal workingTimePercentage);
}