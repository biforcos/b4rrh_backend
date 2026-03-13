package com.b4rrhh.employee.contact.infrastructure.web;

import com.b4rrhh.employee.contact.application.usecase.CreateContactCommand;
import com.b4rrhh.employee.contact.application.usecase.CreateContactUseCase;
import com.b4rrhh.employee.contact.application.usecase.DeleteContactCommand;
import com.b4rrhh.employee.contact.application.usecase.DeleteContactUseCase;
import com.b4rrhh.employee.contact.application.usecase.GetContactByBusinessKeyUseCase;
import com.b4rrhh.employee.contact.application.usecase.ListEmployeeContactsUseCase;
import com.b4rrhh.employee.contact.application.usecase.UpdateContactCommand;
import com.b4rrhh.employee.contact.application.usecase.UpdateContactUseCase;
import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.infrastructure.web.dto.ContactResponse;
import com.b4rrhh.employee.contact.infrastructure.web.dto.CreateContactRequest;
import com.b4rrhh.employee.contact.infrastructure.web.dto.UpdateContactRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/contacts")
public class ContactController {

    private final CreateContactUseCase createContactUseCase;
    private final UpdateContactUseCase updateContactUseCase;
    private final GetContactByBusinessKeyUseCase getContactByBusinessKeyUseCase;
    private final ListEmployeeContactsUseCase listEmployeeContactsUseCase;
    private final DeleteContactUseCase deleteContactUseCase;

    public ContactController(
            CreateContactUseCase createContactUseCase,
            UpdateContactUseCase updateContactUseCase,
            GetContactByBusinessKeyUseCase getContactByBusinessKeyUseCase,
            ListEmployeeContactsUseCase listEmployeeContactsUseCase,
            DeleteContactUseCase deleteContactUseCase
    ) {
        this.createContactUseCase = createContactUseCase;
        this.updateContactUseCase = updateContactUseCase;
        this.getContactByBusinessKeyUseCase = getContactByBusinessKeyUseCase;
        this.listEmployeeContactsUseCase = listEmployeeContactsUseCase;
        this.deleteContactUseCase = deleteContactUseCase;
    }

    @PostMapping
    public ResponseEntity<ContactResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateContactRequest request
    ) {
        Contact created = createContactUseCase.create(
                new CreateContactCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.contactTypeCode(),
                        request.contactValue()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<ContactResponse>> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        List<ContactResponse> response = listEmployeeContactsUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{contactTypeCode}")
    public ResponseEntity<ContactResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String contactTypeCode
    ) {
        return getContactByBusinessKeyUseCase.getByBusinessKey(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        contactTypeCode
                )
                .map(contact -> ResponseEntity.ok(toResponse(contact)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{contactTypeCode}")
    public ResponseEntity<ContactResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String contactTypeCode,
            @RequestBody UpdateContactRequest request
    ) {
        Contact updated = updateContactUseCase.update(
                new UpdateContactCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        contactTypeCode,
                        request.getContactValue()
                )
        );

        return ResponseEntity.ok(toResponse(updated));
    }

    @DeleteMapping("/{contactTypeCode}")
    public ResponseEntity<Void> delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String contactTypeCode
    ) {
        deleteContactUseCase.delete(
                new DeleteContactCommand(ruleSystemCode, employeeTypeCode, employeeNumber, contactTypeCode)
        );
        return ResponseEntity.noContent().build();
    }

    private ContactResponse toResponse(Contact contact) {
        return new ContactResponse(
                contact.getContactTypeCode(),
                contact.getContactValue()
        );
    }
}
