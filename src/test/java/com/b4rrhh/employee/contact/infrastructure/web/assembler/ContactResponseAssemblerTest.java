package com.b4rrhh.employee.contact.infrastructure.web.assembler;

import com.b4rrhh.employee.contact.application.port.ContactCatalogReadPort;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.infrastructure.web.dto.ContactResponse;
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
class ContactResponseAssemblerTest {

    @Mock
    private ContactCatalogReadPort contactCatalogReadPort;

    @Test
    void toResponseEnrichesLabelWhenPresent() {
        ContactResponseAssembler assembler = new ContactResponseAssembler(contactCatalogReadPort);
        Contact contact = contact(10L, "EMAIL", "john.doe@example.com");
        when(contactCatalogReadPort.findContactTypeName("ESP", "EMAIL"))
                .thenReturn(Optional.of("Correo electronico"));

        ContactResponse response = assembler.toResponse("ESP", contact);

        assertEquals("EMAIL", response.contactTypeCode());
        assertEquals("Correo electronico", response.contactTypeName());
        assertEquals("john.doe@example.com", response.contactValue());
    }

    @Test
    void toResponseKeepsCodeAndUsesNullWhenLabelMissing() {
        ContactResponseAssembler assembler = new ContactResponseAssembler(contactCatalogReadPort);
        Contact contact = contact(10L, "EMAIL", "john.doe@example.com");
        when(contactCatalogReadPort.findContactTypeName("ESP", "EMAIL"))
                .thenReturn(Optional.empty());

        ContactResponse response = assembler.toResponse("ESP", contact);

        assertEquals("EMAIL", response.contactTypeCode());
        assertNull(response.contactTypeName());
        assertEquals("john.doe@example.com", response.contactValue());
    }

    private Contact contact(Long employeeId, String contactTypeCode, String contactValue) {
        return new Contact(
                1L,
                employeeId,
                contactTypeCode,
                contactValue,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
