package com.b4rrhh.rulesystem.companyprofile.domain.port;

import com.b4rrhh.rulesystem.companyprofile.domain.model.CompanyProfile;

import java.util.Optional;

public interface CompanyProfileRepository {

    Optional<CompanyProfile> findByCompanyRuleEntityId(Long companyRuleEntityId);

    CompanyProfile save(Long companyRuleEntityId, CompanyProfile companyProfile);
}