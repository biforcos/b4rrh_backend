package com.b4rrhh.employee.identifier.application.service;

import com.b4rrhh.employee.identifier.domain.exception.IdentifierCatalogValueInvalidException;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifierCatalogValidatorTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private IdentifierCatalogValidator validator;

    @BeforeEach
    void setUp() {
        validator = new IdentifierCatalogValidator(ruleEntityRepository);
    }

    @Test
    void normalizesOptionalCode() {
        assertEquals("ESP", validator.normalizeOptionalCode(" esp "));
        assertNull(validator.normalizeOptionalCode("  "));
        assertNull(validator.normalizeOptionalCode(null));
    }

    @Test
    void validatesCountryCodeFromSharedCountryCatalog() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "COUNTRY", "ESP"))
                .thenReturn(Optional.of(activeCountryRuleEntity("ESP", LocalDate.of(1900, 1, 1), null)));

        assertDoesNotThrow(() -> validator.validateCountryCode("ESP", "ESP", LocalDate.of(2026, 1, 1)));
    }

    @Test
    void rejectsCountryCodeOutsideValidityRange() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "COUNTRY", "ESP"))
                .thenReturn(Optional.of(activeCountryRuleEntity("ESP", LocalDate.of(1900, 1, 1), LocalDate.of(2025, 12, 31))));

        assertThrows(
                IdentifierCatalogValueInvalidException.class,
                () -> validator.validateCountryCode("ESP", "ESP", LocalDate.of(2026, 1, 1))
        );
    }

    private RuleEntity activeCountryRuleEntity(String code, LocalDate startDate, LocalDate endDate) {
        return new RuleEntity(
                1L,
                "ESP",
                "COUNTRY",
                code,
                code,
                "Country",
                true,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}