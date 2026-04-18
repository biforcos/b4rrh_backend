package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import com.b4rrhh.rulesystem.workcenter.application.service.WorkCenterInputNormalizer;
import com.b4rrhh.rulesystem.workcenter.application.service.WorkCenterResolver;
import com.b4rrhh.rulesystem.workcenter.application.view.WorkCenterDetails;
import com.b4rrhh.rulesystem.workcenter.domain.exception.WorkCenterNotFoundException;
import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterProfile;
import com.b4rrhh.rulesystem.workcenter.domain.port.WorkCenterProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetWorkCenterServiceTest {

    @Mock private RuleEntityRepository ruleEntityRepository;
    @Mock private WorkCenterProfileRepository workCenterProfileRepository;

    private GetWorkCenterService service;

    @BeforeEach
    void setUp() {
        service = new GetWorkCenterService(
                new WorkCenterResolver(ruleEntityRepository),
                new WorkCenterInputNormalizer(),
                workCenterProfileRepository
        );
    }

    @Test
    void returnsWorkCenterDetailsWithProfile() {
        RuleEntity entity = ruleEntity(10L, "ESP", "MAD-01", "Madrid HQ");
        when(ruleEntityRepository.findApplicableByBusinessKey(eq("ESP"), eq("WORK_CENTER"), eq("MAD-01"), any(LocalDate.class)))
                .thenReturn(Optional.of(entity));
        when(workCenterProfileRepository.findByWorkCenterRuleEntityId(10L))
                .thenReturn(Optional.of(new WorkCenterProfile("ES01", null)));

        WorkCenterDetails result = service.get(new GetWorkCenterQuery("ESP", "MAD-01"));

        assertEquals("ESP", result.workCenter().ruleSystemCode());
        assertEquals("MAD-01", result.workCenter().workCenterCode());
        assertEquals("ES01", result.profile().getCompanyCode());
    }

    @Test
    void returnsEmptyProfileWhenNoneExists() {
        RuleEntity entity = ruleEntity(10L, "ESP", "MAD-01", "Madrid HQ");
        when(ruleEntityRepository.findApplicableByBusinessKey(eq("ESP"), eq("WORK_CENTER"), eq("MAD-01"), any(LocalDate.class)))
                .thenReturn(Optional.of(entity));
        when(workCenterProfileRepository.findByWorkCenterRuleEntityId(10L))
                .thenReturn(Optional.empty());

        WorkCenterDetails result = service.get(new GetWorkCenterQuery("ESP", "MAD-01"));

        assertNull(result.profile().getCompanyCode());
    }

    @Test
    void throwsWhenWorkCenterDoesNotExist() {
        when(ruleEntityRepository.findApplicableByBusinessKey(eq("ESP"), eq("WORK_CENTER"), eq("UNKNOWN"), any(LocalDate.class)))
                .thenReturn(Optional.empty());
        when(ruleEntityRepository.findByFilters(eq("ESP"), eq("WORK_CENTER"), eq("UNKNOWN"), isNull(), isNull()))
                .thenReturn(List.of());

        assertThrows(WorkCenterNotFoundException.class,
                () -> service.get(new GetWorkCenterQuery("ESP", "UNKNOWN")));
    }

    @Test
    void normalizesInputBeforeLookup() {
        RuleEntity entity = ruleEntity(10L, "ESP", "MAD-01", "Madrid HQ");
        when(ruleEntityRepository.findApplicableByBusinessKey(eq("ESP"), eq("WORK_CENTER"), eq("MAD-01"), any(LocalDate.class)))
                .thenReturn(Optional.of(entity));
        when(workCenterProfileRepository.findByWorkCenterRuleEntityId(10L))
                .thenReturn(Optional.empty());

        service.get(new GetWorkCenterQuery(" esp ", " mad-01 "));

        verify(ruleEntityRepository).findApplicableByBusinessKey(eq("ESP"), eq("WORK_CENTER"), eq("MAD-01"), any());
    }

    private RuleEntity ruleEntity(Long id, String ruleSystemCode, String code, String name) {
        return new RuleEntity(id, ruleSystemCode, "WORK_CENTER", code, name, null, true,
                LocalDate.of(2020, 1, 1), null, LocalDateTime.now(), LocalDateTime.now());
    }
}
