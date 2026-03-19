package com.b4rrhh.employee.contract.infrastructure.rest.dto;

public record UpdateContractRequest(
        String contractCode,
        String contractSubtypeCode
) {
}
