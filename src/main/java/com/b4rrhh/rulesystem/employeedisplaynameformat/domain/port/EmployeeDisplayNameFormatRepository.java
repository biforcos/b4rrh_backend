package com.b4rrhh.rulesystem.employeedisplaynameformat.domain.port;

import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;
import java.util.Optional;

public interface EmployeeDisplayNameFormatRepository {
    Optional<EmployeeDisplayNameFormat> findByRuleSystemCode(String ruleSystemCode);
    EmployeeDisplayNameFormat save(EmployeeDisplayNameFormat format);
}
