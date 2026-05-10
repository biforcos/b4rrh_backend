package com.b4rrhh.rulesystem.employeenumbering.domain.port;

import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import java.util.Optional;

public interface EmployeeNumberingConfigRepository {
    Optional<EmployeeNumberingConfig> findByRuleSystemCode(String ruleSystemCode);
    EmployeeNumberingConfig save(EmployeeNumberingConfig config);
}
