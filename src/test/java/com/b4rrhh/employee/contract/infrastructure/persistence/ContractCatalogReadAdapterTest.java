package com.b4rrhh.employee.contract.infrastructure.persistence;

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
class ContractCatalogReadAdapterTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    @Test
    void findContractTypeNameUsesContractTypeAndReturnsLabelWhenFound() {
        ContractCatalogReadAdapter adapter = new ContractCatalogReadAdapter(ruleEntityRepository);
        RuleEntity entity = ruleEntity("ESP", "CONTRACT", "IND", " Indefinido ");
        when(ruleEntityRepository.findByBusinessKey("ESP", "CONTRACT", "IND"))
                .thenReturn(Optional.of(entity));

        Optional<String> result = adapter.findContractTypeName(" esp ", " ind ");

        assertEquals(Optional.of("Indefinido"), result);
        verify(ruleEntityRepository).findByBusinessKey("ESP", "CONTRACT", "IND");
    }

    @Test
    void findContractSubtypeNameReturnsEmptyWhenLabelIsMissing() {
        ContractCatalogReadAdapter adapter = new ContractCatalogReadAdapter(ruleEntityRepository);
        when(ruleEntityRepository.findByBusinessKey("ESP", "CONTRACT_SUBTYPE", "FT1"))
                .thenReturn(Optional.empty());

        Optional<String> result = adapter.findContractSubtypeName("ESP", "FT1");

        assertTrue(result.isEmpty());
        verify(ruleEntityRepository).findByBusinessKey("ESP", "CONTRACT_SUBTYPE", "FT1");
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
