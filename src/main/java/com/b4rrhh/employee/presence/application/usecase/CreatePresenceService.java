package com.b4rrhh.employee.presence.application.usecase;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.port.EmployeePresenceLookupPort;
import com.b4rrhh.employee.presence.application.service.PresenceCatalogValidator;
import com.b4rrhh.employee.presence.domain.exception.ActivePresenceAlreadyExistsException;
import com.b4rrhh.employee.presence.domain.exception.PresenceEmployeeNotFoundException;
import com.b4rrhh.employee.presence.domain.exception.PresenceOverlapException;
import com.b4rrhh.employee.presence.domain.exception.PresenceRuleSystemNotFoundException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.domain.port.PresenceRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreatePresenceService implements CreatePresenceUseCase {

    private final PresenceRepository presenceRepository;
    private final EmployeePresenceLookupPort employeePresenceLookupPort;
    private final RuleSystemRepository ruleSystemRepository;
    private final PresenceCatalogValidator presenceCatalogValidator;

    public CreatePresenceService(
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
    @Transactional
    public Presence create(CreatePresenceCommand command) {
        // Serializes create flow per employee to avoid races in overlap/active/max+1 checks.
        EmployeePresenceContext employee = employeePresenceLookupPort.findByIdForUpdate(command.employeeId())
                .orElseThrow(() -> new PresenceEmployeeNotFoundException(command.employeeId()));

        String ruleSystemCode = employee.ruleSystemCode().trim().toUpperCase();
        ruleSystemRepository.findByCode(ruleSystemCode)
                .orElseThrow(() -> new PresenceRuleSystemNotFoundException(ruleSystemCode));

        String companyCode = presenceCatalogValidator.normalizeRequiredCode("companyCode", command.companyCode());
        String entryReasonCode = presenceCatalogValidator.normalizeRequiredCode("entryReasonCode", command.entryReasonCode());
        String exitReasonCode = presenceCatalogValidator.normalizeOptionalCode(command.exitReasonCode());

        presenceCatalogValidator.validateCompanyCode(ruleSystemCode, companyCode, command.startDate());
        presenceCatalogValidator.validateEntryReasonCode(ruleSystemCode, entryReasonCode, command.startDate());
        if (exitReasonCode != null) {
            presenceCatalogValidator.validateExitReasonCode(ruleSystemCode, exitReasonCode, command.startDate());
        }

        if (command.endDate() == null && presenceRepository.existsActivePresence(command.employeeId())) {
            throw new ActivePresenceAlreadyExistsException(command.employeeId());
        }

        if (presenceRepository.existsOverlappingPeriod(command.employeeId(), command.startDate(), command.endDate())) {
            throw new PresenceOverlapException(command.employeeId());
        }

        int nextPresenceNumber = presenceRepository.findMaxPresenceNumberByEmployeeId(command.employeeId())
                .map(value -> value + 1)
                .orElse(1);

        Presence newPresence = new Presence(
                null,
                command.employeeId(),
                nextPresenceNumber,
                companyCode,
                entryReasonCode,
                exitReasonCode,
                command.startDate(),
                command.endDate(),
                null,
                null
        );

        return presenceRepository.save(newPresence);
    }
}
