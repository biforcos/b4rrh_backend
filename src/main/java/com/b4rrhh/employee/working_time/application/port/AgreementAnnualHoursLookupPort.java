package com.b4rrhh.employee.working_time.application.port;

import java.math.BigDecimal;

public interface AgreementAnnualHoursLookupPort {

    /**
     * Resolve annual hours from the agreement profile for a given agreement business key.
     * The employee/date-specific part (resolving which agreement applies) must be done
     * before calling this method, via {@link EmployeeAgreementContextLookupPort}.
     *
     * @param ruleSystemCode the rule system code (e.g. "ESP")
     * @param agreementCode  the agreement business key (e.g. "99002405011982")
     * @return the annual hours configured in agreement_profile
     * @throws IllegalStateException if the agreement does not exist or profile is missing
     */
    BigDecimal resolveAnnualHours(String ruleSystemCode, String agreementCode);
}
