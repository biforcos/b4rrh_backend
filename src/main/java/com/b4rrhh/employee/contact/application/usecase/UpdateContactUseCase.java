package com.b4rrhh.employee.contact.application.usecase;

import com.b4rrhh.employee.contact.domain.model.Contact;

public interface UpdateContactUseCase {

    Contact update(UpdateContactCommand command);
}
