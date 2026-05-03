package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class UpsertAgreementCategoryProfileService implements UpsertAgreementCategoryProfileUseCase {

    private final AgreementCategoryProfileRepository profileRepository;
    private final RuleEntityRepository ruleEntityRepository;

    public UpsertAgreementCategoryProfileService(
            AgreementCategoryProfileRepository profileRepository,
            RuleEntityRepository ruleEntityRepository
    ) {
        this.profileRepository = profileRepository;
        this.ruleEntityRepository = ruleEntityRepository;
    }

    @Override
    @Transactional
    public AgreementCategoryProfile upsert(UpsertAgreementCategoryProfileCommand command) {
        String ruleSystemCode      = command.ruleSystemCode().trim().toUpperCase();
        String categoryCode        = command.categoryCode().trim().toUpperCase();
        String grupoCotizacionCode = command.grupoCotizacionCode().trim();

        RuleEntity category = ruleEntityRepository
                .findApplicableByBusinessKey(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, LocalDate.now())
                .orElseThrow(() -> {
                    ruleEntityRepository.findByFilters(ruleSystemCode, "AGREEMENT_CATEGORY", categoryCode, null, null);
                    return new AgreementCategoryProfileCategoryNotFoundException(ruleSystemCode, categoryCode);
                });

        ruleEntityRepository
                .findApplicableByBusinessKey(ruleSystemCode, "GRUPO_COTIZACION", grupoCotizacionCode, LocalDate.now())
                .orElseThrow(() -> new GrupoCotizacionInvalidException(ruleSystemCode, grupoCotizacionCode));

        TipoNomina tipoNomina = parseTipoNomina(command.tipoNomina());

        AgreementCategoryProfile requested = new AgreementCategoryProfile(grupoCotizacionCode, tipoNomina);

        return profileRepository.save(category.getId(), requested);
    }

    private TipoNomina parseTipoNomina(String value) {
        if (value == null) throw new IllegalArgumentException("tipoNomina is required");
        try {
            return TipoNomina.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("tipoNomina must be MENSUAL or DIARIO, got: " + value);
        }
    }
}
