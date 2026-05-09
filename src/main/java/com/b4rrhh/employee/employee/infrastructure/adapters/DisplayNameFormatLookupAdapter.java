package com.b4rrhh.employee.employee.infrastructure.adapters;

import com.b4rrhh.employee.employee.application.port.DisplayNameFormatLookupPort;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.port.EmployeeDisplayNameFormatRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class DisplayNameFormatLookupAdapter implements DisplayNameFormatLookupPort {

    private final EmployeeDisplayNameFormatRepository formatRepository;

    public DisplayNameFormatLookupAdapter(EmployeeDisplayNameFormatRepository formatRepository) {
        this.formatRepository = formatRepository;
    }

    @Override
    public Optional<DisplayNameFormatCode> findFormatCodeForRuleSystem(String ruleSystemCode) {
        return formatRepository.findByRuleSystemCode(ruleSystemCode)
                .map(format -> format.formatCode());
    }
}
