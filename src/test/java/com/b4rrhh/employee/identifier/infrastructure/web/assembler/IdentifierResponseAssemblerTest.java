package com.b4rrhh.employee.identifier.infrastructure.web.assembler;

import com.b4rrhh.employee.identifier.application.port.IdentifierCatalogReadPort;
import com.b4rrhh.employee.identifier.domain.model.Identifier;
import com.b4rrhh.employee.identifier.infrastructure.web.dto.IdentifierResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifierResponseAssemblerTest {

    @Mock
    private IdentifierCatalogReadPort identifierCatalogReadPort;

    @Test
    void toResponseEnrichesLabelWhenPresent() {
        IdentifierResponseAssembler assembler = new IdentifierResponseAssembler(identifierCatalogReadPort);
        Identifier identifier = identifier(10L, "NATIONAL_ID", "12345678A", true);
        when(identifierCatalogReadPort.findIdentifierTypeName("ESP", "NATIONAL_ID"))
                .thenReturn(Optional.of("Documento nacional"));

        IdentifierResponse response = assembler.toResponse("ESP", identifier);

        assertEquals("NATIONAL_ID", response.identifierTypeCode());
        assertEquals("Documento nacional", response.identifierTypeName());
        assertEquals("12345678A", response.identifierValue());
    }

    @Test
    void toResponseKeepsCodeAndUsesNullWhenLabelMissing() {
        IdentifierResponseAssembler assembler = new IdentifierResponseAssembler(identifierCatalogReadPort);
        Identifier identifier = identifier(10L, "NATIONAL_ID", "12345678A", true);
        when(identifierCatalogReadPort.findIdentifierTypeName("ESP", "NATIONAL_ID"))
                .thenReturn(Optional.empty());

        IdentifierResponse response = assembler.toResponse("ESP", identifier);

        assertEquals("NATIONAL_ID", response.identifierTypeCode());
        assertNull(response.identifierTypeName());
        assertEquals("12345678A", response.identifierValue());
    }

    private Identifier identifier(Long employeeId, String identifierTypeCode, String identifierValue, boolean isPrimary) {
        return new Identifier(
                1L,
                employeeId,
                identifierTypeCode,
                identifierValue,
                "ESP",
                null,
                isPrimary,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
