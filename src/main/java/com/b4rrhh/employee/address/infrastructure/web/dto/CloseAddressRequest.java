package com.b4rrhh.employee.address.infrastructure.web.dto;

import java.time.LocalDate;

public record CloseAddressRequest(
        LocalDate endDate
) {
}
