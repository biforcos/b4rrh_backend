package com.b4rrhh.employee.identifier.infrastructure.web.assembler;

import com.b4rrhh.employee.identifier.application.port.IdentifierCatalogReadPort;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.infrastructure.web.dto.IdentifierResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IdentifierResponseAssembler {

    private final IdentifierCatalogReadPort identifierCatalogReadPort;

    public IdentifierResponseAssembler(IdentifierCatalogReadPort identifierCatalogReadPort) {
        this.identifierCatalogReadPort = identifierCatalogReadPort;
    }

    public IdentifierResponse toResponse(String ruleSystemCode, Identifier identifier) {
        String identifierTypeName = identifierCatalogReadPort
                .findIdentifierTypeName(ruleSystemCode, identifier.getIdentifierTypeCode())
                .orElse(null);

        return new IdentifierResponse(
                identifier.getIdentifierTypeCode(),
                identifierTypeName,
                identifier.getIdentifierValue(),
                identifier.getIssuingCountryCode(),
                identifier.getExpirationDate(),
                identifier.isPrimary()
        );
    }

    public List<IdentifierResponse> toResponseList(String ruleSystemCode, List<Identifier> identifiers) {
        return identifiers.stream()
                .map(identifier -> toResponse(ruleSystemCode, identifier))
                .toList();
    }
}
