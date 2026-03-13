package com.b4rrhh.employee.contact.domain.port;

import com.b4rrhh.employee.contact.domain.model.Contact;

import java.util.List;
import java.util.Optional;

public interface ContactRepository {

    List<Contact> findByEmployeeIdOrderByContactTypeCode(Long employeeId);

    Optional<Contact> findByEmployeeIdAndContactTypeCode(Long employeeId, String contactTypeCode);

    Contact save(Contact contact);

    void deleteByEmployeeIdAndContactTypeCode(Long employeeId, String contactTypeCode);
}
