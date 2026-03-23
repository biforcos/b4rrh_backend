package com.b4rrhh.employee.lifecycle.infrastructure.rest.dto;

public record RehireContractRequest(
        String contractTypeCode,
        String contractSubtypeCode
) {
}
