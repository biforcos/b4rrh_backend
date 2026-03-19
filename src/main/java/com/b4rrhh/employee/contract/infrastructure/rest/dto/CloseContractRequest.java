package com.b4rrhh.employee.contract.infrastructure.rest.dto;

import java.time.LocalDate;

public record CloseContractRequest(
        LocalDate endDate
) {
}
