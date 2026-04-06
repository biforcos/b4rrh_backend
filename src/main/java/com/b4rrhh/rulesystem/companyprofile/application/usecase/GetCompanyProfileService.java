package com.b4rrhh.rulesystem.companyprofile.application.usecase;

import com.b4rrhh.rulesystem.companyprofile.application.service.CompanyProfileCompanyResolver;
import com.b4rrhh.rulesystem.companyprofile.application.service.CompanyProfileInputNormalizer;
import com.b4rrhh.rulesystem.companyprofile.domain.exception.CompanyProfileNotFoundException;
import com.b4rrhh.rulesystem.companyprofile.domain.model.CompanyProfile;
import com.b4rrhh.rulesystem.companyprofile.domain.port.CompanyProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetCompanyProfileService implements GetCompanyProfileUseCase {

    private final CompanyProfileRepository companyProfileRepository;
    private final CompanyProfileCompanyResolver companyProfileCompanyResolver;
    private final CompanyProfileInputNormalizer companyProfileInputNormalizer;

    public GetCompanyProfileService(
            CompanyProfileRepository companyProfileRepository,
            CompanyProfileCompanyResolver companyProfileCompanyResolver,
            CompanyProfileInputNormalizer companyProfileInputNormalizer
    ) {
        this.companyProfileRepository = companyProfileRepository;
        this.companyProfileCompanyResolver = companyProfileCompanyResolver;
        this.companyProfileInputNormalizer = companyProfileInputNormalizer;
    }

    @Override
    @Transactional(readOnly = true)
    public CompanyProfile get(GetCompanyProfileQuery query) {
        String ruleSystemCode = companyProfileInputNormalizer.normalizeRequiredRuleSystemCode(query.ruleSystemCode());
        String companyCode = companyProfileInputNormalizer.normalizeRequiredCompanyCode(query.companyCode());

        RuleEntity company = companyProfileCompanyResolver.resolveApplicableToday(ruleSystemCode, companyCode);

        return companyProfileRepository
                .findByCompanyRuleEntityId(company.getId())
                .orElseThrow(() -> new CompanyProfileNotFoundException(ruleSystemCode, companyCode));
    }
}