package com.b4rrhh.employee.presence.infrastructure.persistence;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceCatalogReadAdapterTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    @Test
    void findCompanyNameUsesEmployeePresenceCompanyTypeAndReturnsLabelWhenFound() {
        PresenceCatalogReadAdapter adapter = new PresenceCatalogReadAdapter(ruleEntityRepository);
        RuleEntity entity = ruleEntity("ESP", "EMPLOYEE_PRESENCE_COMPANY", "AC01", " Empresa Activa ");
        when(ruleEntityRepository.findByBusinessKey("ESP", "EMPLOYEE_PRESENCE_COMPANY", "AC01"))
                .thenReturn(Optional.of(entity));

        Optional<String> result = adapter.findCompanyName(" esp ", " ac01 ");

        assertEquals(Optional.of("Empresa Activa"), result);
        verify(ruleEntityRepository).findByBusinessKey("ESP", "EMPLOYEE_PRESENCE_COMPANY", "AC01");
    }

    @Test
    void findCompanyNameReturnsEmptyWhenLabelIsMissing() {
        PresenceCatalogReadAdapter adapter = new PresenceCatalogReadAdapter(ruleEntityRepository);
        when(ruleEntityRepository.findByBusinessKey("ESP", "EMPLOYEE_PRESENCE_COMPANY", "AC99"))
                .thenReturn(Optional.empty());

        Optional<String> result = adapter.findCompanyName("ESP", "AC99");

        assertTrue(result.isEmpty());
        verify(ruleEntityRepository).findByBusinessKey("ESP", "EMPLOYEE_PRESENCE_COMPANY", "AC99");
    }

    private RuleEntity ruleEntity(String ruleSystemCode, String ruleEntityTypeCode, String code, String name) {
        return new RuleEntity(
                1L,
                ruleSystemCode,
                ruleEntityTypeCode,
                code,
                name,
                null,
                true,
                LocalDate.of(2020, 1, 1),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
