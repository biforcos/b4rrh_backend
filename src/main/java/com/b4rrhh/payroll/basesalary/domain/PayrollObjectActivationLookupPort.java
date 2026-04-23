package com.b4rrhh.payroll.basesalary.domain;

/**
 * Output port to verify object activation semantics in payroll object activation table.
 */
public interface PayrollObjectActivationLookupPort {

    boolean isActive(
            String ruleSystemCode,
            String ownerTypeCode,
            String ownerCode,
            String targetObjectTypeCode,
            String targetObjectCode
    );
}
