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
        List<RuleEntity> expected = List.of(ruleEntity("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01"));
        when(ruleEntityRepository.findByFilters("ESP", null, null, null)).thenReturn(expected);

        List<RuleEntity> result = service.list(new ListRuleEntitiesQuery("esp", null, null, null));

        assertEquals(1, result.size());
        assertEquals("ESP", result.get(0).getRuleSystemCode());
        verify(ruleEntityRepository).findByFilters("ESP", null, null, null);
    }

    @Test
    void listsByRuleSystemCodeAndRuleEntityTypeCode() {
        List<RuleEntity> expected = List.of(ruleEntity("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01"));
        when(ruleEntityRepository.findByFilters("ESP", "EMPLOYEE_PRESENCE_COMPANY", null, null)).thenReturn(expected);

        List<RuleEntity> result = service.list(new ListRuleEntitiesQuery("ESP", "employee_presence_company", null, null));

        assertEquals(1, result.size());
        assertEquals("EMPLOYEE_PRESENCE_COMPANY", result.get(0).getRuleEntityTypeCode());
        verify(ruleEntityRepository).findByFilters("ESP", "EMPLOYEE_PRESENCE_COMPANY", null, null);
    }

    @Test
    void listsByRuleSystemCodeRuleEntityTypeCodeAndCode() {
        List<RuleEntity> expected = List.of(ruleEntity("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01"));
        when(ruleEntityRepository.findByFilters("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", null)).thenReturn(expected);

        List<RuleEntity> result = service.list(
                new ListRuleEntitiesQuery("ESP", "EMPLOYEE_PRESENCE_COMPANY", "es01", null)
        );

        assertEquals(1, result.size());
        assertEquals("ES01", result.get(0).getCode());
        verify(ruleEntityRepository).findByFilters("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", null);
    }

    @Test
    void returnsEmptyListWhenNoResults() {
        when(ruleEntityRepository.findByFilters("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES99", null))
                .thenReturn(List.of());

        List<RuleEntity> result = service.list(
                new ListRuleEntitiesQuery("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES99", null)
        );

        assertEquals(0, result.size());
    }

    @Test
    void normalizesFiltersBeforeSearching() {
        when(ruleEntityRepository.findByFilters("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", true))
                .thenReturn(List.of(ruleEntity("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01")));

        service.list(new ListRuleEntitiesQuery(" esp ", " employee_presence_company ", " es01 ", true));

        verify(ruleEntityRepository).findByFilters("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", true);
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
