package com.b4rrhh.employee.employee.application;

import com.b4rrhh.employee.employee.application.port.DisplayNameFormatLookupPort;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatter;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DisplayNameComputationService {

    private final DisplayNameFormatLookupPort formatLookupPort;

    public DisplayNameComputationService(DisplayNameFormatLookupPort formatLookupPort) {
        this.formatLookupPort = formatLookupPort;
    }

    public String compute(
            String ruleSystemCode,
            String firstName,
            String lastName1,
            String lastName2,
            String preferredName) {

        if (preferredName != null && !preferredName.isBlank()) {
            return preferredName.trim();
        }

        return formatLookupPort.findFormatCodeForRuleSystem(ruleSystemCode)
                .map(code -> DisplayNameFormatter.format(firstName, lastName1, lastName2, code))
                .orElseGet(() -> Stream.of(firstName, lastName1, lastName2)
                        .filter(s -> s != null && !s.isBlank())
                        .map(String::trim)
                        .collect(Collectors.joining(" ")));
    }
}
