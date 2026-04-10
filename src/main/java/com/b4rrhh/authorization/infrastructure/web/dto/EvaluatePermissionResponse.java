package com.b4rrhh.authorization.infrastructure.web.dto;

public record EvaluatePermissionResponse(
        String decision,
        String reason
) {}
