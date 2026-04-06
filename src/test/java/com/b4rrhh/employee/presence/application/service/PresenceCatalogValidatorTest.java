package com.b4rrhh.employee.presence.application.service;

import com.b4rrhh.employee.presence.domain.exception.PresenceCatalogValueInvalidException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceCatalogValidatorTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private PresenceCatalogValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PresenceCatalogValidator(ruleEntityRepository);
    }

    @Test
    void normalizesRequiredAndOptionalCodes() {
        assertEquals("AC01", validator.normalizeRequiredCode("companyCode", " ac01 "));
        assertEquals("ENT01", validator.normalizeOptionalCode(" ent01 "));
        assertEquals(null, validator.normalizeOptionalCode("   "));
    }

    @Test
    void validatesInclusiveEndDateSemantics() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "COMPANY", "AC01"))
                .thenReturn(Optional.of(activeRuleEntityWithDates(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))));

        // Inclusive end date: referenceDate == endDate is valid.
        assertDoesNotThrow(() -> validator.validateCompanyCode("ESP", "AC01", LocalDate.of(2026, 1, 31)));
    }

    @Test
    void rejectsReferenceDateAfterEndDate() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "COMPANY", "AC01"))
                .thenReturn(Optional.of(activeRuleEntityWithDates(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))));

        assertThrows(
                PresenceCatalogValueInvalidException.class,
                () -> validator.validateCompanyCode("ESP", "AC01", LocalDate.of(2026, 2, 1))
        );
    }

    private RuleEntity activeRuleEntityWithDates(LocalDate startDate, LocalDate endDate) {
        return new RuleEntity(
                1L,
                "ESP",
            "COMPANY",
                "AC01",
                "Company",
                null,
                true,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
