package com.b4rrhh.employee.contact.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataContactRepository extends JpaRepository<ContactEntity, Long> {

    List<ContactEntity> findByEmployeeIdOrderByContactTypeCodeAsc(Long employeeId);

    Optional<ContactEntity> findByEmployeeIdAndContactTypeCode(Long employeeId, String contactTypeCode);

    void deleteByEmployeeIdAndContactTypeCode(Long employeeId, String contactTypeCode);
}
