package com.b4rrhh.rulesystem.employeenumbering.domain.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EmployeeNumberingConfigTest {

    @Test
    void formatsNumberWithPrefixAndPadding() {
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 6, 1, 1L);
        assertEquals("EMP000001", config.formatNumber());
    }

    @Test
    void formatsNumberWithEmptyPrefixAndPadding() {
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "", 8, 1, 42L);
        assertEquals("00000042", config.formatNumber());
    }

    @Test
    void detectsOverflowWhenNextValueExceedsMax() {
        // numericPartLength=3 → max=999; nextValue=1000 overflows
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 3, 1, 1000L);
        assertTrue(config.isExhausted());
    }

    @Test
    void doesNotOverflowWhenNextValueEqualsMax() {
        // numericPartLength=3 → max=999; nextValue=999 is fine
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 3, 1, 999L);
        assertFalse(config.isExhausted());
    }

    @Test
    void advancedByStepReturnsNewConfig() {
        EmployeeNumberingConfig config = new EmployeeNumberingConfig("ESP", "EMP", 6, 3, 1L);
        EmployeeNumberingConfig advanced = config.advance();
        assertEquals(4L, advanced.nextValue());
    }
}
