package com.b4rrhh.employee.working_time.domain.service;

import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;

import java.math.BigDecimal;

public interface WorkingTimeDerivationPolicy {

    /**
     * Derive working time hours from percentage and annual hours.
     * Annual hours come from agreement_profile and must be resolved before calling this.
     *
     * @param workingTimePercentage the percentage of full-time (0-100)
     * @param annualHours           the annual hours for the full-time employee (from agreement_profile)
     * @return the derived hours for weekly, daily, and monthly periods
     */
    WorkingTimeDerivedHours derive(BigDecimal workingTimePercentage, BigDecimal annualHours);
}