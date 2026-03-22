package com.b4rrhh.rulesystem.application.usecase;

import com.b4rrhh.rulesystem.domain.exception.RuleEntityNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetRuleEntityByBusinessKeyServiceTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private GetRuleEntityByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new GetRuleEntityByBusinessKeyService(ruleEntityRepository);
    }

    @Test
    void returnsOccurrenceWhenItExists() {
        LocalDate startDate = LocalDate.of(1900, 1, 1);
        RuleEntity entity = ruleEntity(startDate, null);

        when(ruleEntityRepository.findByBusinessKeyAndStartDate("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", startDate))
                .thenReturn(Optional.of(entity));

        RuleEntity result = service.get(new GetRuleEntityByBusinessKeyQuery("esp", "employee_presence_company", "es01", startDate));

        assertEquals("ESP", result.getRuleSystemCode());
        assertEquals("EMPLOYEE_PRESENCE_COMPANY", result.getRuleEntityTypeCode());
        assertEquals("ES01", result.getCode());
        assertEquals(startDate, result.getStartDate());
        verify(ruleEntityRepository).findByBusinessKeyAndStartDate("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", startDate);
    }

    @Test
    void throwsNotFoundWhenOccurrenceDoesNotExist() {
        LocalDate startDate = LocalDate.of(1900, 1, 1);
        when(ruleEntityRepository.findByBusinessKeyAndStartDate("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", startDate))
                .thenReturn(Optional.empty());

        assertThrows(
                RuleEntityNotFoundException.class,
                () -> service.get(new GetRuleEntityByBusinessKeyQuery("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", startDate))
        );
    }

    private RuleEntity ruleEntity(LocalDate startDate, LocalDate endDate) {
        return new RuleEntity(
                1L,
                "ESP",
                "EMPLOYEE_PRESENCE_COMPANY",
                "ES01",
                "Company",
                null,
                endDate == null,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
