package com.b4rrhh.employee.presence.infrastructure.web.assembler;

import com.b4rrhh.employee.presence.application.port.PresenceCatalogReadPort;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.infrastructure.web.dto.PresenceResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceResponseAssemblerTest {

    @Mock
    private PresenceCatalogReadPort presenceCatalogReadPort;

    @Test
    void toResponseEnrichesCompanyLabelWhenPresent() {
        PresenceResponseAssembler assembler = new PresenceResponseAssembler(presenceCatalogReadPort);
        Presence presence = presence(1, "AC01", "ENT01", "EXT01");
        when(presenceCatalogReadPort.findCompanyName("ESP", "AC01"))
                .thenReturn(Optional.of("Empresa Activa"));
        when(presenceCatalogReadPort.findEntryReasonName("ESP", "ENT01"))
            .thenReturn(Optional.of("Alta inicial"));
        when(presenceCatalogReadPort.findExitReasonName("ESP", "EXT01"))
            .thenReturn(Optional.of("Baja voluntaria"));

        PresenceResponse response = assembler.toResponse("ESP", presence);

        assertEquals(1, response.presenceNumber());
        assertEquals("AC01", response.companyCode());
        assertEquals("Empresa Activa", response.companyName());
        assertEquals("Alta inicial", response.entryReasonName());
        assertEquals("Baja voluntaria", response.exitReasonName());
    }

    @Test
    void toResponseKeepsCompanyCodeAndUsesNullWhenLabelMissing() {
        PresenceResponseAssembler assembler = new PresenceResponseAssembler(presenceCatalogReadPort);
        Presence presence = presence(1, "AC01", "ENT01", "EXT01");
        when(presenceCatalogReadPort.findCompanyName("ESP", "AC01"))
                .thenReturn(Optional.empty());
        when(presenceCatalogReadPort.findEntryReasonName("ESP", "ENT01"))
                .thenReturn(Optional.empty());
        when(presenceCatalogReadPort.findExitReasonName("ESP", "EXT01"))
                .thenReturn(Optional.empty());

        PresenceResponse response = assembler.toResponse("ESP", presence);

        assertEquals("AC01", response.companyCode());
        assertNull(response.companyName());
        assertNull(response.entryReasonName());
        assertNull(response.exitReasonName());
    }

    private Presence presence(int presenceNumber, String companyCode) {
        return presence(presenceNumber, companyCode, "ENT01", null);
    }

    private Presence presence(int presenceNumber, String companyCode, String entryReasonCode, String exitReasonCode) {
        return new Presence(
                20L,
                10L,
                presenceNumber,
                companyCode,
                entryReasonCode,
                exitReasonCode,
                LocalDate.of(2026, 1, 10),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
