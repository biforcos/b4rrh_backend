package com.b4rrhh.payroll.application.port;

public record EmployeeTaxInfoContext(
    String familySituation,
    int descendantsCount,
    int ascendantsCount,
    String disabilityDegree,
    boolean pensionCompensatoria,
    boolean geographicMobility,
    boolean habitualResidenceLoan,
    String taxTerritory
) {
    public static EmployeeTaxInfoContext ofDefault() {
        return new EmployeeTaxInfoContext("SINGLE_OR_OTHER", 0, 0, "NONE", false, false, false, "COMUN");
    }
}
