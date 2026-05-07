package com.b4rrhh.employee.tax_information.infrastructure.persistence;

import com.b4rrhh.employee.tax_information.application.port.TaxInfoPresenceLookupPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class TaxInfoPresenceLookupAdapter implements TaxInfoPresenceLookupPort {

    private final JdbcTemplate jdbc;

    public TaxInfoPresenceLookupAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean isPresenceStartDate(Long employeeId, LocalDate date) {
        Integer count = jdbc.queryForObject(
            "SELECT COUNT(*) FROM employee.presence WHERE employee_id = ? AND start_date = ?",
            Integer.class, employeeId, date);
        return count != null && count > 0;
    }
}
