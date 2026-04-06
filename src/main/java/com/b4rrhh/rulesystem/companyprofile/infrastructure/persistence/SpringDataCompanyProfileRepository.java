package com.b4rrhh.rulesystem.companyprofile.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SpringDataCompanyProfileRepository extends JpaRepository<CompanyProfileEntity, Long> {

    Optional<CompanyProfileEntity> findByCompanyRuleEntityId(Long companyRuleEntityId);
}