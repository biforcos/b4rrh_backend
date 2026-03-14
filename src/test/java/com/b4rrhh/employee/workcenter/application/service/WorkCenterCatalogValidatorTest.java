package com.b4rrhh.employee.workcenter.application.service;

import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
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
class WorkCenterCatalogValidatorTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private WorkCenterCatalogValidator validator;

    @BeforeEach
    void setUp() {
        validator = new WorkCenterCatalogValidator(ruleEntityRepository);
    }

    @Test
    void normalizesRequiredCode() {
        assertEquals("MADRID_HQ", validator.normalizeRequiredCode("workCenterCode", " madrid_hq "));
    }

    @Test
    void validatesWorkCenterCodeWithInclusiveEndDateSemantics() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "WORK_CENTER", "MADRID_HQ"))
                .thenReturn(Optional.of(activeRuleEntityWithDates(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))));

        assertDoesNotThrow(() -> validator.validateWorkCenterCode("ESP", "MADRID_HQ", LocalDate.of(2026, 1, 31)));
    }

    @Test
    void rejectsWorkCenterCodeOutsideValidityRange() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "WORK_CENTER", "MADRID_HQ"))
                .thenReturn(Optional.of(activeRuleEntityWithDates(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31))));

        assertThrows(
                WorkCenterCatalogValueInvalidException.class,
                () -> validator.validateWorkCenterCode("ESP", "MADRID_HQ", LocalDate.of(2026, 2, 1))
        );
    }

    private RuleEntity activeRuleEntityWithDates(LocalDate startDate, LocalDate endDate) {
        return new RuleEntity(
                1L,
                "ESP",
                "WORK_CENTER",
                "MADRID_HQ",
                "Madrid HQ",
                null,
                true,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}