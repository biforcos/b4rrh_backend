package com.b4rrhh.authorization.application.usecase;

import java.util.List;

public record EvaluatePermissionCommand(
        String subject,
        List<String> roleCodes,
        String resourceCode,
        String actionCode
) {}
