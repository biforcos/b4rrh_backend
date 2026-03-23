package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

import java.time.LocalDate;

public record RehireEmployeeRequest(
        LocalDate rehireDate,
        String entryReasonCode,
        String companyCode,
        RehireLaborClassificationRequest laborClassification,
        RehireContractRequest contract,
        RehireWorkCenterRequest workCenter
) {
}
