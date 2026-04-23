package com.b4rrhh.rulesystem.agreementprofile.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class AgreementProfileTest {

    @Test
    void constructorValidatesRequiredFields() {
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile(null, "Display", null, new BigDecimal("1560"), true)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile("", "Display", null, new BigDecimal("1560"), true)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile("CA-001", null, null, new BigDecimal("1560"), true)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile("CA-001", "Display", null, null, true)
        );
    }

    @Test
    void constructorValidatesAnnualHoursRange() {
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile("CA-001", "Display", null, new BigDecimal("0"), true)
        );
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile("CA-001", "Display", null, new BigDecimal("10000"), true)
        );
    }

    @Test
    void constructorValidatesFieldLengths() {
        String tooLongNumber = "A".repeat(51);
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile(tooLongNumber, "Display", null, new BigDecimal("1560"), true)
        );

        String tooLongDisplay = "A".repeat(201);
        assertThrows(IllegalArgumentException.class, () ->
                new AgreementProfile("CA-001", tooLongDisplay, null, new BigDecimal("1560"), true)
        );
    }

    @Test
    void constructorTrimsWhitespace() {
        AgreementProfile profile = new AgreementProfile(
                "  CA-001  ",
                "  Display Name  ",
                "  SHORT  ",
                new BigDecimal("1560"),
                true
        );

        assertEquals("CA-001", profile.getOfficialAgreementNumber());
        assertEquals("Display Name", profile.getDisplayName());
        assertEquals("SHORT", profile.getShortName());
    }

    @Test
    void updateReturnsNewInstance() {
        AgreementProfile original = new AgreementProfile(
                "CA-001",
                "Original",
                "ORG",
                new BigDecimal("1560"),
                true
        );

        AgreementProfile updated = original.update(
                "CA-002",
                "Updated",
                "UPD",
                new BigDecimal("1680"),
                false
        );

        assertNotSame(original, updated);
        assertEquals("CA-001", original.getOfficialAgreementNumber());
        assertEquals("CA-002", updated.getOfficialAgreementNumber());
        assertTrue(original.isActive());
        assertFalse(updated.isActive());
    }

    @Test
    void shortNameCanBeNullOrEmpty() {
        AgreementProfile profile1 = new AgreementProfile(
                "CA-001",
                "Display",
                null,
                new BigDecimal("1560"),
                true
        );
        assertNull(profile1.getShortName());

        AgreementProfile profile2 = new AgreementProfile(
                "CA-001",
                "Display",
                "  ",
                new BigDecimal("1560"),
                true
        );
        assertNull(profile2.getShortName());
    }
}
