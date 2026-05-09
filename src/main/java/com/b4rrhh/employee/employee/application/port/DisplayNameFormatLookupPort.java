package com.b4rrhh.employee.employee.application.port;

import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import java.util.Optional;

public interface DisplayNameFormatLookupPort {
    Optional<DisplayNameFormatCode> findFormatCodeForRuleSystem(String ruleSystemCode);
}
