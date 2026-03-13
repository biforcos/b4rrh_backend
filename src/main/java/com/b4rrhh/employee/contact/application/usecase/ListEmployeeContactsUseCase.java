package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.domain.model.Contact;

import java.util.List;

public interface ListEmployeeContactsUseCase {

    List<Contact> listByEmployeeBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
