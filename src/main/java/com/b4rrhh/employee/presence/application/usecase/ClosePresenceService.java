package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.application.service.PresenceCatalogValidator;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;

@Service
public class ClosePresenceService implements ClosePresenceUseCase {

    private final PresenceRepository presenceRepository;
    private final EmployeePresenceLookupPort employeePresenceLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final PresenceCatalogValidator presenceCatalogValidator;

    public ClosePresenceService(
            PresenceRepository presenceRepository,
            EmployeePresenceLookupPort employeePresenceLookupPort,
            RuleSystemRepository ruleSystemRepository,
            PresenceCatalogValidator presenceCatalogValidator
    ) {
        this.presenceRepository = presenceRepository;
        this.employeePresenceLookupPort = employeePresenceLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
        this.presenceCatalogValidator = presenceCatalogValidator;
    }

    @Override
    public Presence close(ClosePresenceCommand command) {
        EmployeePresenceContext employee = employeePresenceLookupPort.findById(command.employeeId())
                .orElseThrow(() -> new PresenceEmployeeNotFoundException(command.employeeId()));

        Presence existing = presenceRepository.findByIdAndEmployeeId(command.presenceId(), command.employeeId())
                .orElseThrow(() -> new PresenceNotFoundException(command.employeeId(), command.presenceId()));

        String ruleSystemCode = employee.ruleSystemCode().trim().toUpperCase();
        ruleSystemRepository.findByCode(ruleSystemCode)
                .orElseThrow(() -> new PresenceRuleSystemNotFoundException(ruleSystemCode));

        String normalizedExitReasonCode = presenceCatalogValidator.normalizeOptionalCode(command.exitReasonCode());
        if (normalizedExitReasonCode != null) {
            presenceCatalogValidator.validateExitReasonCode(ruleSystemCode, normalizedExitReasonCode, command.endDate());
        }

        Presence closed = existing.close(command.endDate(), normalizedExitReasonCode);
        return presenceRepository.save(closed);
    }
}
