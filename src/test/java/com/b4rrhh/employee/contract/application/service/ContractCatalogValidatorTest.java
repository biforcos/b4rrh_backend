package com.b4rrhh.employee.contract.application.service;

import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
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
    void acceptsContractSubtypeCodeOfAnyLengthBeforeCatalogLookup() {
        // Subtypes have no fixed-length constraint; "01" (2-char) and "ABCD" (4-char) are both accepted
        assertEquals("01",   validator.normalizeRequiredCode("contractSubtypeCode", "01"));
        assertEquals("AB",   validator.normalizeRequiredCode("contractSubtypeCode", "AB"));
        assertEquals("ABCD", validator.normalizeRequiredCode("contractSubtypeCode", "ABCD"));
        verifyNoInteractions(ruleEntityRepository);
    }

    @Test
    void normalizesValidCodes() {
        assertEquals("IND", validator.normalizeRequiredCode("contractCode", " ind "));
        assertEquals("01",  validator.normalizeRequiredCode("contractSubtypeCode", " 01 "));
        verifyNoInteractions(ruleEntityRepository);
    }
}
