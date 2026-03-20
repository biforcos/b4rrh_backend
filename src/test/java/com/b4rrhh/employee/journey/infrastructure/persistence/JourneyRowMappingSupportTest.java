package com.b4rrhh.employee.journey.infrastructure.persistence;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JourneyRowMappingSupportTest {

    @Test
    void toLocalDateReturnsNullForNull() {
        assertNull(JourneyRowMappingSupport.toLocalDate(null));
    }

    @Test
    void toLocalDateSupportsLocalDateAndSqlDateAndUtilDate() {
        LocalDate reference = LocalDate.of(2026, 3, 20);

        assertEquals(reference, JourneyRowMappingSupport.toLocalDate(reference));
        assertEquals(reference, JourneyRowMappingSupport.toLocalDate(java.sql.Date.valueOf(reference)));

        Date utilDate = Date.from(reference.atStartOfDay(ZoneId.systemDefault()).toInstant());
        assertEquals(reference, JourneyRowMappingSupport.toLocalDate(utilDate));
    }

    @Test
    void toLocalDateThrowsForUnsupportedType() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> JourneyRowMappingSupport.toLocalDate("2026-03-20")
        );

        assertEquals("Unsupported date value: java.lang.String", exception.getMessage());
    }

    @Test
    void toBigDecimalReturnsNullForNull() {
        assertNull(JourneyRowMappingSupport.toBigDecimal(null));
    }

    @Test
    void toBigDecimalSupportsBigDecimalAndNumber() {
        assertEquals(new BigDecimal("12.50"), JourneyRowMappingSupport.toBigDecimal(new BigDecimal("12.50")));
        assertEquals(new BigDecimal("50"), JourneyRowMappingSupport.toBigDecimal(50));
        assertEquals(new BigDecimal("12.5"), JourneyRowMappingSupport.toBigDecimal(12.5d));
    }

    @Test
    void toBigDecimalThrowsForUnsupportedType() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> JourneyRowMappingSupport.toBigDecimal("50")
        );

        assertEquals("Unsupported numeric value: java.lang.String", exception.getMessage());
    }

    @Test
    void requireColumnsReturnsColumnsWhenRowMatchesExpectedShape() {
        Object[] row = new Object[]{"A", "B", "C"};

        Object[] result = JourneyRowMappingSupport.requireColumns(row, 3, "journey contract query");

        assertArrayEquals(row, result);
    }

    @Test
    void requireColumnsThrowsWhenRowIsNotArray() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> JourneyRowMappingSupport.requireColumns("invalid", 3, "journey contract query")
        );

        assertEquals(
                "Unexpected row type for journey contract query: java.lang.String",
                exception.getMessage()
        );
    }

    @Test
    void requireColumnsThrowsWhenRowHasFewerColumnsThanExpected() {
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> JourneyRowMappingSupport.requireColumns(new Object[]{"A"}, 2, "journey contract query")
        );

        assertEquals(
                "Unexpected row shape for journey contract query: expected at least 2 columns but found 1",
                exception.getMessage()
        );
    }
}