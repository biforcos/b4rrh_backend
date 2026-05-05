package com.b4rrhh.payroll.domain.model;

import java.util.Set;

public final class PayrollTypeCodes {

    public static final String NORMAL = "NORMAL";
    public static final String EXTRA  = "EXTRA";

    private static final Set<String> VALID = Set.of(NORMAL, EXTRA);

    private PayrollTypeCodes() {}

    public static boolean isValid(String code) {
        return code != null && VALID.contains(code);
    }
}
