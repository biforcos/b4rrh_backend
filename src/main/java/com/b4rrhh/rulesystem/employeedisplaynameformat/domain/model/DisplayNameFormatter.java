package com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model;

import java.util.Arrays;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DisplayNameFormatter {

    private DisplayNameFormatter() {}

    public static String format(
            String firstName, String lastName1, String lastName2,
            DisplayNameFormatCode formatCode) {

        Objects.requireNonNull(formatCode, "formatCode must not be null");

        String fn = blank(firstName) ? "" : firstName.trim();
        String ln1 = blank(lastName1) ? "" : lastName1.trim();
        String ln2 = blank(lastName2) ? "" : lastName2.trim();

        return switch (formatCode) {
            case FULL_TITLE_CASE -> joinNonEmpty(" ", toTitleCase(fn), toTitleCase(ln1), toTitleCase(ln2));
            case FULL_UPPER -> joinNonEmpty(" ", fn, ln1, ln2).toUpperCase(Locale.ROOT);
            case SURNAME_FIRST_UPPER -> surnameFirst(fn, ln1, ln2, false);
            case SHORT_TITLE -> joinNonEmpty(" ", toTitleCase(fn), toTitleCase(ln1));
            case SHORT_UPPER -> joinNonEmpty(" ", fn, ln1).toUpperCase(Locale.ROOT);
            case SURNAME_ABBREV_UPPER -> surnameFirst(fn, ln1, ln2, true);
        };
    }

    private static String surnameFirst(String fn, String ln1, String ln2, boolean abbreviateFirst) {
        String surnames = joinNonEmpty(" ", ln1, ln2).toUpperCase(Locale.ROOT);
        String firstPart = abbreviateFirst ? abbreviate(fn) : fn.toUpperCase(Locale.ROOT);
        if (firstPart.isEmpty()) return surnames;
        if (surnames.isEmpty()) return firstPart;
        return surnames + ", " + firstPart;
    }

    /** "Juan Antonio" -> "J.A." */
    private static String abbreviate(String name) {
        if (blank(name)) return "";
        return Arrays.stream(name.trim().split("\\s+"))
                .filter(w -> !w.isEmpty())
                .map(w -> Character.toUpperCase(w.charAt(0)) + ".")
                .collect(Collectors.joining());
    }

    /** Title-cases every word: "juan antonio" -> "Juan Antonio" */
    private static String toTitleCase(String input) {
        if (blank(input)) return "";
        return Arrays.stream(input.trim().split("\\s+"))
                .filter(w -> !w.isEmpty())
                .map(w -> Character.toUpperCase(w.charAt(0)) + w.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }

    private static String joinNonEmpty(String sep, String... parts) {
        return Stream.of(parts)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.joining(sep));
    }

    private static boolean blank(String s) {
        return s == null || s.isBlank();
    }
}
