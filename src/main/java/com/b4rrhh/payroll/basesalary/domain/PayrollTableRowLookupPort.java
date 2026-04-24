package com.b4rrhh.payroll.basesalary.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Output port for resolving payroll table rows.
 * Answers: "What is the salary value for this table and search key on this date?"
 */
public interface PayrollTableRowLookupPort {

    /**
     * Resolve the monthly base salary from a table row.
     *
     * @param ruleSystemCode the rule system code (e.g., "ESP")
     * @param tableCode the table code (e.g., "SB_99002405011982")
     * @param searchCode the search key (e.g., category code "99002405-G2")
     * @param effectiveDate the date for which to find the applicable row
         * @return the monthly value, or empty if not found
     */
        Optional<BigDecimal> resolveMonthlyValue(
            String ruleSystemCode,
            String tableCode,
            String searchCode,
            LocalDate effectiveDate
    );

    Optional<BigDecimal> resolveDailyValue(
            String ruleSystemCode,
            String tableCode,
            String searchCode,
            LocalDate effectiveDate
    );
}
