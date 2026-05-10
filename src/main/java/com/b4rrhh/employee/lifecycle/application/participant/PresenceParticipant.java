package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.application.port.HireParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
import org.springframework.stereotype.Component;

@Component
public class PresenceParticipant implements HireParticipant {

    private final CreatePresenceUseCase createPresenceUseCase;

    public PresenceParticipant(CreatePresenceUseCase createPresenceUseCase) {
        this.createPresenceUseCase = createPresenceUseCase;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void participate(HireContext ctx) {
        try {
            ctx.setPresence(createPresenceUseCase.create(new CreatePresenceCommand(
                    ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                    ctx.companyCode(), ctx.entryReasonCode(), null, ctx.hireDate(), null
            )));
        } catch (PresenceCatalogValueInvalidException ex) {
            throw new HireEmployeeCatalogValueInvalidException(ex.getMessage(), ex);
        }
    }
}
