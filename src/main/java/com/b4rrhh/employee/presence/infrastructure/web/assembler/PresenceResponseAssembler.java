package com.b4rrhh.employee.presence.infrastructure.web.assembler;

import com.b4rrhh.employee.presence.application.port.PresenceCatalogReadPort;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.infrastructure.web.dto.PresenceResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PresenceResponseAssembler {

    private final PresenceCatalogReadPort presenceCatalogReadPort;

    public PresenceResponseAssembler(PresenceCatalogReadPort presenceCatalogReadPort) {
        this.presenceCatalogReadPort = presenceCatalogReadPort;
    }

    public PresenceResponse toResponse(String ruleSystemCode, Presence presence) {
        String companyName = presenceCatalogReadPort
                .findCompanyName(ruleSystemCode, presence.getCompanyCode())
                .orElse(null);

        return new PresenceResponse(
                presence.getPresenceNumber(),
                presence.getCompanyCode(),
                companyName,
                presence.getEntryReasonCode(),
                presence.getExitReasonCode(),
                presence.getStartDate(),
                presence.getEndDate()
        );
    }

    public List<PresenceResponse> toResponseList(String ruleSystemCode, List<Presence> presences) {
        return presences.stream()
                .map(presence -> toResponse(ruleSystemCode, presence))
                .toList();
    }
}
