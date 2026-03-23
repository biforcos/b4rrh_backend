package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

public record HireContractRequest(
        String contractTypeCode,
        String contractSubtypeCode
) {
}
