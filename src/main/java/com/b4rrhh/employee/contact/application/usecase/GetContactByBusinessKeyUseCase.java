package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.domain.model.Contact;

import java.util.Optional;

public interface GetContactByBusinessKeyUseCase {

    Optional<Contact> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String contactTypeCode
    );
}
