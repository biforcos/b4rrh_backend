package com.b4rrhh.employee.tax_information.application.port;

import java.time.LocalDate;

public interface TaxInfoPresenceLookupPort {
    boolean isPresenceStartDate(Long employeeId, LocalDate date);
}
