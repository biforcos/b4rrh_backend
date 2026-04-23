package com.b4rrhh.payroll.basesalary.domain;

import java.util.Optional;

/**
 * Output port for resolving payroll object bindings.
 * Answers: "Which object (e.g., salary table) is bound to this owner for this role?"
 */
public interface PayrollObjectBindingLookupPort {

    /**
     * Resolve the bound object code for a given owner and binding role.
     *
     * @param ruleSystemCode the rule system code (e.g., "ESP")
     * @param ownerTypeCode the owner type (e.g., "AGREEMENT")
     * @param ownerCode the owner code (e.g., agreement code)
     * @param bindingRoleCode the binding role (e.g., "BASE_SALARY_TABLE")
         * @return the bound object code (e.g., table code), or empty if not found
     */
        Optional<String> resolveBoundObjectCode(
            String ruleSystemCode,
            String ownerTypeCode,
            String ownerCode,
            String bindingRoleCode
    );
}
