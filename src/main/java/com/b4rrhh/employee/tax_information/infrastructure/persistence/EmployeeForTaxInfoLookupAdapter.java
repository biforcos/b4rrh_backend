package com.b4rrhh.employee.tax_information.infrastructure.persistence;

import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;

@Component
public class EmployeeForTaxInfoLookupAdapter implements EmployeeForTaxInfoLookupPort {

    private final JdbcTemplate jdbc;

    public EmployeeForTaxInfoLookupAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Optional<Long> findEmployeeId(String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        List<Long> ids = jdbc.queryForList(
            "SELECT id FROM employee.employee WHERE rule_system_code = ? AND employee_type_code = ? AND employee_number = ?",
            Long.class, ruleSystemCode, employeeTypeCode, employeeNumber);
        return ids.isEmpty() ? Optional.empty() : Optional.of(ids.get(0));
    }
}
