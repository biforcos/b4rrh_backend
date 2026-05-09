package com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase;

import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.port.EmployeeDisplayNameFormatRepository;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Optional;

@Service
public class GetEmployeeDisplayNameFormatService implements GetEmployeeDisplayNameFormatUseCase {

    private final EmployeeDisplayNameFormatRepository repository;

    public GetEmployeeDisplayNameFormatService(EmployeeDisplayNameFormatRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<EmployeeDisplayNameFormat> getByRuleSystemCode(String ruleSystemCode) {
        return repository.findByRuleSystemCode(ruleSystemCode.trim().toUpperCase(Locale.ROOT));
    }
}
