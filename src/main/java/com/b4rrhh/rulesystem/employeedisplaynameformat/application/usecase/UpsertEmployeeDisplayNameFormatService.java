package com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase;

import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.port.EmployeeDisplayNameFormatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UpsertEmployeeDisplayNameFormatService implements UpsertEmployeeDisplayNameFormatUseCase {

    private final EmployeeDisplayNameFormatRepository formatRepository;
    private final RuleSystemRepository ruleSystemRepository;

    public UpsertEmployeeDisplayNameFormatService(
            EmployeeDisplayNameFormatRepository formatRepository,
            RuleSystemRepository ruleSystemRepository) {
        this.formatRepository = formatRepository;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    @Transactional
    public EmployeeDisplayNameFormat upsert(UpsertEmployeeDisplayNameFormatCommand command) {
        String normalizedCode = command.ruleSystemCode().trim().toUpperCase(Locale.ROOT);

        ruleSystemRepository.findByCode(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rule system not found: " + normalizedCode));

        DisplayNameFormatCode formatCode;
        try {
            formatCode = DisplayNameFormatCode.valueOf(
                    command.formatCode().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Unknown formatCode: " + command.formatCode() +
                    ". Valid values: FULL_TITLE_CASE, FULL_UPPER, SURNAME_FIRST_UPPER, SHORT_TITLE, SHORT_UPPER, SURNAME_ABBREV_UPPER");
        }

        return formatRepository.save(new EmployeeDisplayNameFormat(normalizedCode, formatCode));
    }
}
