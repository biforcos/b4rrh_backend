package com.b4rrhh.authorization.infrastructure.web.dto;

public record EvaluatePermissionRequest(
        String resourceCode,
        String actionCode
) {}
