package com.b4rrhh.payroll.application.port;

import java.util.Optional;

public interface WorkCenterProfileLookupPort {
    Optional<WorkCenterProfileContext> findByRuleSystemAndCode(String ruleSystemCode, String workCenterCode);
}
