package com.b4rrhh.employee.contact.infrastructure.web.assembler;

import com.b4rrhh.employee.contact.application.port.ContactCatalogReadPort;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.infrastructure.web.dto.ContactResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContactResponseAssembler {

    private final ContactCatalogReadPort contactCatalogReadPort;

    public ContactResponseAssembler(ContactCatalogReadPort contactCatalogReadPort) {
        this.contactCatalogReadPort = contactCatalogReadPort;
    }

    public ContactResponse toResponse(String ruleSystemCode, Contact contact) {
        String contactTypeName = contactCatalogReadPort
                .findContactTypeName(ruleSystemCode, contact.getContactTypeCode())
                .orElse(null);

        return new ContactResponse(
                contact.getContactTypeCode(),
                contactTypeName,
                contact.getContactValue()
        );
    }

    public List<ContactResponse> toResponseList(String ruleSystemCode, List<Contact> contacts) {
        return contacts.stream()
                .map(contact -> toResponse(ruleSystemCode, contact))
                .toList();
    }
}
