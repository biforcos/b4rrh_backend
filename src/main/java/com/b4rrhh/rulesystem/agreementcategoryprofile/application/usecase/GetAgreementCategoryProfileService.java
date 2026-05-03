package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class GetAgreementCategoryProfileService implements GetAgreementCategoryProfileUseCase {

    private final AgreementCategoryProfileRepository profileRepository;
    private final RuleEntityRepository ruleEntityRepository;

    public GetAgreementCategoryProfileService(
            AgreementCategoryProfileRepository profileRepository,
            RuleEntityRepository ruleEntityRepository
    ) {
        this.profileRepository = profileRepository;
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AgreementCategoryProfile get(GetAgreementCategoryProfileQuery query) {
        String ruleSystemCode = query.ruleSystemCode().trim().toUpperCase();
        String categoryCode   = query.categoryCode().trim().toUpperCase();

        RuleEntity category = ruleEntityRepository
                .findApplicableByBusinessKey(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, LocalDate.now())
                .orElseThrow(() -> {
                    ruleEntityRepository.findByFilters(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, null, null);
                    return new AgreementCategoryProfileCategoryNotFoundException(ruleSystemCode, categoryCode);
                });

        return profileRepository.findByCategoryRuleEntityId(category.getId())
                .orElseThrow(() -> new AgreementCategoryProfileNotFoundException(ruleSystemCode, categoryCode));
    }
}
