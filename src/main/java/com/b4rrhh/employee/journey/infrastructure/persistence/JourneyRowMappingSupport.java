package com.b4rrhh.employee.journey.infrastructure.persistence;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

final class JourneyRowMappingSupport {

    private JourneyRowMappingSupport() {
    }

    static LocalDate toLocalDate(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Date date) {
            return date.toLocalDate();
        }
        if (value instanceof java.util.Date date) {
            return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        throw new IllegalStateException("Unsupported date value: " + value.getClass().getName());
    }

    static BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return new BigDecimal(number.toString());
        }

        throw new IllegalStateException("Unsupported numeric value: " + value.getClass().getName());
    }

    static Object[] requireColumns(Object row, int minColumns, String context) {
        if (!(row instanceof Object[] columns)) {
            throw new IllegalStateException("Unexpected row type for " + context + ": " + typeName(row));
        }

        if (columns.length < minColumns) {
            throw new IllegalStateException(
                    "Unexpected row shape for " + context + ": expected at least "
                            + minColumns
                            + " columns but found "
                            + columns.length
            );
        }

        return columns;
    }

    private static String typeName(Object value) {
        return value == null ? "null" : value.getClass().getName();
    }
}