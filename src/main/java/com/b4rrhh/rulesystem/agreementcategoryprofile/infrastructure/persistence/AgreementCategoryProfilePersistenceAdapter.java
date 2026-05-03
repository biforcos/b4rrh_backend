package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.persistence;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AgreementCategoryProfilePersistenceAdapter implements AgreementCategoryProfileRepository {

    private final SpringDataAgreementCategoryProfileRepository springDataRepository;

    public AgreementCategoryProfilePersistenceAdapter(
            SpringDataAgreementCategoryProfileRepository springDataRepository
    ) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<AgreementCategoryProfile> findByCategoryRuleEntityId(Long categoryRuleEntityId) {
        return springDataRepository.findByAgreementCategoryRuleEntityId(categoryRuleEntityId)
                .map(this::toDomain);
    }

    @Override
    public AgreementCategoryProfile save(Long categoryRuleEntityId, AgreementCategoryProfile profile) {
        AgreementCategoryProfileEntity entity = springDataRepository
                .findByAgreementCategoryRuleEntityId(categoryRuleEntityId)
                .orElseGet(AgreementCategoryProfileEntity::new);

        entity.setAgreementCategoryRuleEntityId(categoryRuleEntityId);
        entity.setGrupoCotizacionCode(profile.getGrupoCotizacionCode());
        entity.setTipoNomina(profile.getTipoNomina().name());

        return toDomain(springDataRepository.save(entity));
    }

    private AgreementCategoryProfile toDomain(AgreementCategoryProfileEntity entity) {
        return new AgreementCategoryProfile(
                entity.getGrupoCotizacionCode(),
                TipoNomina.valueOf(entity.getTipoNomina())
        );
    }
}
