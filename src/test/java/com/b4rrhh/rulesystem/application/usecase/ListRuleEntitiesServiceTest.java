package com.b4rrhh.rulesystem.application.usecase;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRuleEntitiesServiceTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private ListRuleEntitiesService service;

    @BeforeEach
    void setUp() {
        service = new ListRuleEntitiesService(ruleEntityRepository);
    }

    @Test
    void listsByRuleSystemCode() {
        List<RuleEntity> expected = List.of(ruleEntity("ESP", "COMPANY", "ES01"));
        when(ruleEntityRepository.findByFilters("ESP", null, null, null, null)).thenReturn(expected);

        List<RuleEntity> result = service.list(new ListRuleEntitiesQuery("esp", null, null, null, null));

        assertEquals(1, result.size());
        assertEquals("ESP", result.get(0).getRuleSystemCode());
        verify(ruleEntityRepository).findByFilters("ESP", null, null, null, null);
    }

    @Test
    void listsByRuleSystemCodeAndRuleEntityTypeCode() {
        List<RuleEntity> expected = List.of(ruleEntity("ESP", "COMPANY", "ES01"));
        when(ruleEntityRepository.findByFilters("ESP", "COMPANY", null, null, null)).thenReturn(expected);

        List<RuleEntity> result = service.list(new ListRuleEntitiesQuery("ESP", "company", null, null, null));

        assertEquals(1, result.size());
        assertEquals("COMPANY", result.get(0).getRuleEntityTypeCode());
        verify(ruleEntityRepository).findByFilters("ESP", "COMPANY", null, null, null);
    }

    @Test
    void listsByRuleSystemCodeRuleEntityTypeCodeAndCode() {
        List<RuleEntity> expected = List.of(ruleEntity("ESP", "COMPANY", "ES01"));
        when(ruleEntityRepository.findByBusinessKey("ESP", "COMPANY", "ES01"))
            .thenReturn(expected.stream().findFirst());

        List<RuleEntity> result = service.list(
            new ListRuleEntitiesQuery("ESP", "COMPANY", "es01", null, null)
        );

        assertEquals(1, result.size());
        assertEquals("ES01", result.get(0).getCode());
        verify(ruleEntityRepository).findByBusinessKey("ESP", "COMPANY", "ES01");
    }

    @Test
    void returnsEmptyListWhenNoResults() {
        when(ruleEntityRepository.findByBusinessKey("ESP", "COMPANY", "ES99"))
            .thenReturn(java.util.Optional.empty());

        List<RuleEntity> result = service.list(
            new ListRuleEntitiesQuery("ESP", "COMPANY", "ES99", null, null)
        );

        assertEquals(0, result.size());
    }

    @Test
    void normalizesFiltersBeforeSearching() {
        when(ruleEntityRepository.findByFilters("ESP", "COMPANY", "ES01", true, null))
            .thenReturn(List.of(ruleEntity("ESP", "COMPANY", "ES01")));

        service.list(new ListRuleEntitiesQuery(" esp ", " company ", " es01 ", true, null));

        verify(ruleEntityRepository).findByFilters("ESP", "COMPANY", "ES01", true, null);
        }

        @Test
        void forwardsReferenceDateWhenProvided() {
        LocalDate referenceDate = LocalDate.of(2026, 3, 1);
        when(ruleEntityRepository.findByFilters("ESP", "AGREEMENT", null, true, referenceDate))
            .thenReturn(List.of(ruleEntity("ESP", "AGREEMENT", "AGR01")));

        service.list(new ListRuleEntitiesQuery("esp", "agreement", null, true, referenceDate));

        verify(ruleEntityRepository).findByFilters("ESP", "AGREEMENT", null, true, referenceDate);
    }

    private RuleEntity ruleEntity(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        return new RuleEntity(
                1L,
                ruleSystemCode,
                ruleEntityTypeCode,
                code,
                "Name",
                null,
                true,
                LocalDate.of(2020, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
