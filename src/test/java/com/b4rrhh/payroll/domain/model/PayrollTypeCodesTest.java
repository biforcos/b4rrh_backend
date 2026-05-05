package com.b4rrhh.payroll.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollTypeCodesTest {

    @Test
    void normalIsValid() {
        assertTrue(PayrollTypeCodes.isValid("NORMAL"));
    }

    @Test
    void extraIsValid() {
        assertTrue(PayrollTypeCodes.isValid("EXTRA"));
    }

    @Test
    void ordIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid("ORD"));
    }

    @Test
    void mensualIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid("MENSUAL"));
    }

    @Test
    void nullIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid(null));
    }

    @Test
    void emptyIsNotValid() {
        assertFalse(PayrollTypeCodes.isValid(""));
    }
}
