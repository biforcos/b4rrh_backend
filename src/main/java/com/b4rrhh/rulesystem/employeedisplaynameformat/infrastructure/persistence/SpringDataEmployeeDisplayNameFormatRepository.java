package com.b4rrhh.rulesystem.employeedisplaynameformat.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SpringDataEmployeeDisplayNameFormatRepository
        extends JpaRepository<EmployeeDisplayNameFormatEntity, Long> {

    Optional<EmployeeDisplayNameFormatEntity> findByRuleSystemCode(String ruleSystemCode);
}
