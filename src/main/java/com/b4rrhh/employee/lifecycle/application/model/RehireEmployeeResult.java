package com.b4rrhh.employee.lifecycle.application.model;

import java.time.LocalDate;

public record RehireEmployeeResult(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate rehireDate,
        String status,
        Integer newPresenceNumber,
        String newPresenceCompanyCode,
        String newPresenceEntryReasonCode,
        LocalDate newPresenceStartDate,
        String newContractTypeCode,
        String newContractSubtypeCode,
        LocalDate newContractStartDate,
        String newAgreementCode,
        String newAgreementCategoryCode,
        LocalDate newLaborClassificationStartDate,
        Integer newWorkCenterAssignmentNumber,
        String newWorkCenterCode,
        LocalDate newWorkCenterStartDate,
        boolean created
) {
}