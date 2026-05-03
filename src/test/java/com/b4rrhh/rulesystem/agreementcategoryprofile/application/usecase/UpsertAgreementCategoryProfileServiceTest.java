package com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.port.AgreementCategoryProfileRepository;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UpsertAgreementCategoryProfileServiceTest {

    @Mock private AgreementCategoryProfileRepository profileRepository;
    @Mock private RuleEntityRepository ruleEntityRepository;

    private UpsertAgreementCategoryProfileService service;

    @BeforeEach
    void setUp() {
        service = new UpsertAgreementCategoryProfileService(profileRepository, ruleEntityRepository);
    }

    @Test
    void createsProfileWhenItDoesNotExist() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "05", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(99L, "ESP", "GRUPO_COTIZACION", "05")));
        when(profileRepository.save(any(Long.class), any(AgreementCategoryProfile.class)))
                .thenAnswer(inv -> inv.getArgument(1));

        AgreementCategoryProfile result = service.upsert(
                new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "05", "MENSUAL"));

        ArgumentCaptor<AgreementCategoryProfile> captor = ArgumentCaptor.forClass(AgreementCategoryProfile.class);
        verify(profileRepository).save(eq(42L), captor.capture());
        assertEquals("05", captor.getValue().getGrupoCotizacionCode());
        assertEquals(TipoNomina.MENSUAL, captor.getValue().getTipoNomina());
        assertEquals("05", result.getGrupoCotizacionCode());
    }

    @Test
    void updatesExistingProfile() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "07", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(88L, "ESP", "GRUPO_COTIZACION", "07")));
        when(profileRepository.save(any(Long.class), any(AgreementCategoryProfile.class)))
                .thenAnswer(inv -> inv.getArgument(1));

        AgreementCategoryProfile result = service.upsert(
                new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "07", "MENSUAL"));

        assertEquals("07", result.getGrupoCotizacionCode());
    }

    @Test
    void throwsWhenCategoryNotFound() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "GHOST", LocalDate.now()))
                .thenReturn(Optional.empty());
        when(ruleEntityRepository.findByFilters("ESP", "AGREEMENT_CATEGORY", "GHOST", null, null))
                .thenReturn(List.of());

        assertThrows(AgreementCategoryProfileCategoryNotFoundException.class,
                () -> service.upsert(new UpsertAgreementCategoryProfileCommand("ESP", "GHOST", "05", "MENSUAL")));
    }

    @Test
    void throwsWhenGrupoCotizacionNotFound() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "99", LocalDate.now()))
                .thenReturn(Optional.empty());

        assertThrows(GrupoCotizacionInvalidException.class,
                () -> service.upsert(new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "99", "MENSUAL")));
    }

    @Test
    void throwsWhenTipoNominaIsInvalid() {
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(42L, "ESP", "AGREEMENT_CATEGORY", "CAT_ADMIN")));
        when(ruleEntityRepository.findApplicableByBusinessKey("ESP", "GRUPO_COTIZACION", "05", LocalDate.now()))
                .thenReturn(Optional.of(ruleEntity(99L, "ESP", "GRUPO_COTIZACION", "05")));

        assertThrows(IllegalArgumentException.class,
                () -> service.upsert(new UpsertAgreementCategoryProfileCommand("ESP", "CAT_ADMIN", "05", "SEMANAL")));
    }

    private RuleEntity ruleEntity(Long id, String ruleSystemCode, String typeCode, String code) {
        return new RuleEntity(id, ruleSystemCode, typeCode, code, code, null, true,
                LocalDate.of(1900, 1, 1), null, LocalDateTime.now(), LocalDateTime.now());
    }
}
