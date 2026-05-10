package com.b4rrhh.employee.lifecycle.infrastructure;

import com.b4rrhh.employee.lifecycle.application.port.NextEmployeeNumberPort;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigNotFoundException;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingExhaustedException;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence.EmployeeNumberingConfigEntity;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence.SpringDataEmployeeNumberingConfigRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NextEmployeeNumberAdapter implements NextEmployeeNumberPort {

    private final SpringDataEmployeeNumberingConfigRepository repository;

    public NextEmployeeNumberAdapter(SpringDataEmployeeNumberingConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public String consumeNext(String ruleSystemCode) {
        EmployeeNumberingConfigEntity config = repository.findByRuleSystemCodeForUpdate(ruleSystemCode)
                .orElseThrow(() -> new EmployeeNumberingConfigNotFoundException(ruleSystemCode));

        long value = config.getNextValue();
        long max = (long) Math.pow(10, config.getNumericPartLength()) - 1;
        if (value > max) {
            throw new EmployeeNumberingExhaustedException(ruleSystemCode);
        }

        String number = config.getPrefix() + String.format("%0" + config.getNumericPartLength() + "d", value);
        config.setNextValue(value + config.getStep());
        repository.save(config);
        return number;
    }
}
