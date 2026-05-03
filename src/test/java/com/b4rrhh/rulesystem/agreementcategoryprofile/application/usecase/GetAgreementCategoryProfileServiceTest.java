package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAgreementCategoryProfileServiceTest {

    @Mock private AgreementCategoryProfileRepository profileRepository;
    @Mock private RuleEntityRepository ruleEntityRepository;

    private GetAgreementCategoryProfileService service;

    @BeforeEach
    void setUp() {
        service = new GetAgreementCategoryProfileService(profileRepository, ruleEntityRepository);
    }

    @Test
    void returnsProfileWhenItExists() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(profileRepository.findByCategoryRuleEntityId(42L))
                .thenReturn(Optional.of(new AgreementCategoryProfile("05", TipoNomina.MENSUAL)));

        AgreementCategoryProfile result = service.get(new GetAgreementCategoryProfileQuery("ESP", "CAT_ADMIN"));

        assertEquals("05", result.getGrupoCotizacionCode());
        assertEquals(TipoNomina.MENSUAL, result.getTipoNomina());
    }

    @Test
    void throwsNotFoundWhenCategoryDoesNotExist() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "UNKNOWN", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(ruleEntityRepository.findByFilters("ESP", "AGREEMENT_CATEGORY", "UNKNOWN", null, null))
                .thenReturn(List.of());

        assertThrows(AgreementCategoryProfileCategoryNotFoundException.class,
                () -> service.get(new GetAgreementCategoryProfileQuery("ESP", "UNKNOWN")));
    }

    @Test
    void throwsProfileNotFoundWhenProfileDoesNotExist() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(profileRepository.findByCategoryRuleEntityId(42L))
                .thenReturn(Optional.empty());

        assertThrows(AgreementCategoryProfileNotFoundException.class,
                () -> service.get(new GetAgreementCategoryProfileQuery("ESP", "CAT_ADMIN")));
    }

    private RuleEntity ruleEntity(Long id, String ruleSystemCode, String typeCode, String code) {
        return new RuleEntity(id, ruleSystemCode, typeCode, code, code, null, true,
                LocalDate.of(1900, 1, 1), null, LocalDateTime.now(), LocalDateTime.now());
    }
}
