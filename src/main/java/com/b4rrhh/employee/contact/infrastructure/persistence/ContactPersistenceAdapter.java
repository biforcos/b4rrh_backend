package com.b4rrhh.employee.contact.infrastructure.persistence;

import com.b4rrhh.employee.contact.domain.model.Contact;
import com.b4rrhh.employee.contact.domain.port.ContactRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class ContactPersistenceAdapter implements ContactRepository {

    private final SpringDataContactRepository springDataContactRepository;

    public ContactPersistenceAdapter(SpringDataContactRepository springDataContactRepository) {
        this.springDataContactRepository = springDataContactRepository;
    }

    @Override
    public List<Contact> findByEmployeeIdOrderByContactTypeCode(Long employeeId) {
        return springDataContactRepository.findByEmployeeIdOrderByContactTypeCodeAsc(employeeId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Contact> findByEmployeeIdAndContactTypeCode(Long employeeId, String contactTypeCode) {
        return springDataContactRepository.findByEmployeeIdAndContactTypeCode(employeeId, contactTypeCode)
                .map(this::toDomain);
    }

    @Override
    public Contact save(Contact contact) {
        ContactEntity entity = toEntity(contact);
        ContactEntity saved = springDataContactRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByEmployeeIdAndContactTypeCode(Long employeeId, String contactTypeCode) {
        springDataContactRepository.deleteByEmployeeIdAndContactTypeCode(employeeId, contactTypeCode);
    }

    private Contact toDomain(ContactEntity entity) {
        return new Contact(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getContactTypeCode(),
                entity.getContactValue(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    private ContactEntity toEntity(Contact contact) {
        ContactEntity entity = new ContactEntity();
        entity.setId(contact.getId());
        entity.setEmployeeId(contact.getEmployeeId());
        entity.setContactTypeCode(contact.getContactTypeCode());
        entity.setContactValue(contact.getContactValue());
        entity.setCreatedAt(contact.getCreatedAt());
        entity.setUpdatedAt(contact.getUpdatedAt());
        return entity;
    }
}
