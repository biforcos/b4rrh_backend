package com.b4rrhh.rulesystem.employeenumbering.application.usecase;

import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigInvalidException;
import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.domain.port.EmployeeNumberingConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class UpsertEmployeeNumberingConfigService implements UpsertEmployeeNumberingConfigUseCase {

    private final EmployeeNumberingConfigRepository configRepository;
    private final RuleSystemRepository ruleSystemRepository;

    public UpsertEmployeeNumberingConfigService(
            EmployeeNumberingConfigRepository configRepository,
            RuleSystemRepository ruleSystemRepository) {
        this.configRepository = configRepository;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    @Transactional
    public EmployeeNumberingConfig upsert(UpsertEmployeeNumberingConfigCommand command) {
        String ruleSystemCode = command.ruleSystemCode().trim().toUpperCase(Locale.ROOT);

        ruleSystemRepository.findByCode(ruleSystemCode)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Rule system not found: " + ruleSystemCode));

        if (command.prefix().length() + command.numericPartLength() > 15) {
            throw new EmployeeNumberingConfigInvalidException(
                    "prefix.length() + numericPartLength must be <= 15 (employee_number is varchar(15)); " +
                    "got prefix=\"" + command.prefix() + "\" (" + command.prefix().length() +
                    ") + numericPartLength=" + command.numericPartLength());
        }

        return configRepository.save(new EmployeeNumberingConfig(
                ruleSystemCode,
                command.prefix(),
                command.numericPartLength(),
                command.step(),
                command.nextValue()
        ));
    }
}
