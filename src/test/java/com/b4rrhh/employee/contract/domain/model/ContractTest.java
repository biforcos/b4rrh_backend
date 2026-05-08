package com.b4rrhh.employee.contract.domain.model;

import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ContractTest {

    @Test
    void rejectsContractCodeShorterThanThree() {
        assertThrows(
                ContractInvalidException.class,
                () -> new Contract(10L, "AB", "FT1", LocalDate.of(2026, 1, 1), null)
        );
    }

    @Test
    void rejectsContractCodeLongerThanThree() {
        assertThrows(
                ContractInvalidException.class,
                () -> new Contract(10L, "ABCD", "FT1", LocalDate.of(2026, 1, 1), null)
        );
    }

    @Test
    void updateRejectsContractCodeWithInvalidLength() {
        Contract contract = new Contract(10L, "IND", "FT1", LocalDate.of(2026, 1, 1), null);

        assertThrows(
                ContractInvalidException.class,
                () -> contract.updateContract("AB", "FT1")
        );
    }

}
