package com.b4rrhh.employee.contract.application.service;

import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.rulesystem.domain.port.RuleEntityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ContractCatalogValidatorTest {

    @Mock
    private RuleEntityRepository ruleEntityRepository;

    private ContractCatalogValidator validator;

    @BeforeEach
    void setUp() {
        validator = new ContractCatalogValidator(ruleEntityRepository);
    }

    @Test
    void rejectsContractCodeShorterThanThreeBeforeCatalogLookup() {
        assertThrows(
                ContractInvalidException.class,
                () -> validator.normalizeRequiredCode("contractCode", "AB")
        );

        verifyNoInteractions(ruleEntityRepository);
    }

    @Test
    void rejectsContractCodeLongerThanThreeBeforeCatalogLookup() {
        assertThrows(
                ContractInvalidException.class,
                () -> validator.normalizeRequiredCode("contractCode", "ABCD")
        );

        verifyNoInteractions(ruleEntityRepository);
    }

    @Test
    void rejectsContractSubtypeCodeShorterThanThreeBeforeCatalogLookup() {
        assertThrows(
                ContractSubtypeInvalidException.class,
                () -> validator.normalizeRequiredCode("contractSubtypeCode", "AB")
        );

        verifyNoInteractions(ruleEntityRepository);
    }

    @Test
    void rejectsContractSubtypeCodeLongerThanThreeBeforeCatalogLookup() {
        assertThrows(
                ContractSubtypeInvalidException.class,
                () -> validator.normalizeRequiredCode("contractSubtypeCode", "ABCD")
        );

        verifyNoInteractions(ruleEntityRepository);
    }

    @Test
    void normalizesValidThreeCharacterCodes() {
        assertEquals("IND", validator.normalizeRequiredCode("contractCode", " ind "));
        assertEquals("FT1", validator.normalizeRequiredCode("contractSubtypeCode", " ft1 "));
        verifyNoInteractions(ruleEntityRepository);
    }
}
