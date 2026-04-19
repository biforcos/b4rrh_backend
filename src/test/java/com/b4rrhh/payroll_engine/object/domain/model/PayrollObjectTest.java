package com.b4rrhh.payroll_engine.object.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PayrollObjectTest {

    @Test
    void equalityIsBasedOnBusinessKeyOnly() {
        PayrollObject a = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null);
        PayrollObject b = new PayrollObject(99L, "ESP", PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void differentObjectCodeProducesInequality() {
        PayrollObject a = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null);
        PayrollObject b = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.CONCEPT, "IRPF", null, null);

        assertNotEquals(a, b);
    }

    @Test
    void differentObjectTypeProducesInequality() {
        PayrollObject a = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.CONCEPT, "BASE", null, null);
        PayrollObject b = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.TABLE, "BASE", null, null);

        assertNotEquals(a, b);
    }

    @Test
    void differentRuleSystemProducesInequality() {
        PayrollObject a = new PayrollObject(1L, "ESP", PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null);
        PayrollObject b = new PayrollObject(1L, "FRA", PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null);

        assertNotEquals(a, b);
    }

    @Test
    void technicalIdNotRequiredForEquality() {
        PayrollObject withId = new PayrollObject(5L, "ESP", PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null);
        PayrollObject withoutId = new PayrollObject(null, "ESP", PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null);

        assertEquals(withId, withoutId);
    }

    @Test
    void nullRuleSystemCodeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new PayrollObject(null, null, PayrollObjectTypeCode.CONCEPT, "SALBASE", null, null));
    }

    @Test
    void nullObjectTypeCodeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new PayrollObject(null, "ESP", null, "SALBASE", null, null));
    }

    @Test
    void nullObjectCodeThrows() {
        assertThrows(IllegalArgumentException.class,
                () -> new PayrollObject(null, "ESP", PayrollObjectTypeCode.CONCEPT, null, null, null));
    }
}
