package com.b4rrhh.employee.lifecycle.application.model;

import java.time.LocalDate;

public record TerminateEmployeeResult(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        LocalDate terminationDate,
        String exitReasonCode,
        String status,
        Integer closedPresenceNumber,
        String closedPresenceCompanyCode,
        String closedPresenceEntryReasonCode,
        String closedPresenceExitReasonCode,
        LocalDate closedPresenceStartDate,
        LocalDate closedPresenceEndDate,
        String closedContractTypeCode,
        String closedContractSubtypeCode,
        LocalDate closedContractStartDate,
        LocalDate closedContractEndDate,
        String closedAgreementCode,
        String closedAgreementCategoryCode,
        LocalDate closedLaborClassificationStartDate,
        LocalDate closedLaborClassificationEndDate,
        Integer closedWorkCenterAssignmentNumber,
        String closedWorkCenterCode,
        LocalDate closedWorkCenterStartDate,
        LocalDate closedWorkCenterEndDate
) {
}
