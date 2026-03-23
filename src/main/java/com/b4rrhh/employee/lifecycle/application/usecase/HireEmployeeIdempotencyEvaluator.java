package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeConflictException;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Component
public class HireEmployeeIdempotencyEvaluator {

    public record HireEmployeeIdempotencyInput(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String firstName,
            String lastName1,
            String lastName2,
            String preferredName,
            LocalDate hireDate,
            String companyCode,
            String entryReasonCode,
            String agreementCode,
            String agreementCategoryCode,
            String contractTypeCode,
            String contractSubtypeCode,
            String workCenterCode
    ) {
    }

    public HireEmployeeResult evaluateOrThrow(
            HireEmployeeIdempotencyInput input,
            HireEmployeeCurrentState currentState
    ) {
        Employee employee = currentState.employee();
        requireEquals(employee.getFirstName(), input.firstName(), "firstName");
        requireEquals(employee.getLastName1(), input.lastName1(), "lastName1");
        requireEquals(employee.getLastName2(), input.lastName2(), "lastName2");
        requireEquals(employee.getPreferredName(), input.preferredName(), "preferredName");

        if (!"ACTIVE".equals(employee.getStatus())) {
            throw new HireEmployeeConflictException("existing employee status is not ACTIVE for idempotent hire retry");
        }

        Presence matchingPresence = requireSingleActivePresence(input.hireDate(), currentState.presences());
        if (!input.companyCode().equals(matchingPresence.getCompanyCode())) {
            throw new HireEmployeeConflictException("existing initial presence companyCode is not equivalent");
        }
        if (!input.entryReasonCode().equals(matchingPresence.getEntryReasonCode())) {
            throw new HireEmployeeConflictException("existing initial presence entryReasonCode is not equivalent");
        }

        LaborClassification matchingLaborClassification = requireSingleActiveLaborClassification(
                input.hireDate(),
                currentState.laborClassifications()
        );
        if (!input.agreementCode().equals(matchingLaborClassification.getAgreementCode())) {
            throw new HireEmployeeConflictException("existing initial laborClassification agreementCode is not equivalent");
        }
        if (!input.agreementCategoryCode().equals(matchingLaborClassification.getAgreementCategoryCode())) {
            throw new HireEmployeeConflictException("existing initial laborClassification agreementCategoryCode is not equivalent");
        }

        Contract matchingContract = requireSingleActiveContract(input.hireDate(), currentState.contracts());
        if (!input.contractTypeCode().equals(matchingContract.getContractCode())) {
            throw new HireEmployeeConflictException("existing initial contract contractTypeCode is not equivalent");
        }
        if (!input.contractSubtypeCode().equals(matchingContract.getContractSubtypeCode())) {
            throw new HireEmployeeConflictException("existing initial contract contractSubtypeCode is not equivalent");
        }

        WorkCenter matchingWorkCenter = requireSingleActiveWorkCenter(input.hireDate(), currentState.workCenters());
        if (!input.workCenterCode().equals(matchingWorkCenter.getWorkCenterCode())) {
            throw new HireEmployeeConflictException("existing initial workCenter workCenterCode is not equivalent");
        }

        return new HireEmployeeResult(
                input.ruleSystemCode(),
                input.employeeTypeCode(),
                input.employeeNumber(),
                employee.getFirstName(),
                employee.getLastName1(),
                employee.getLastName2(),
                employee.getPreferredName(),
                employee.getStatus(),
                input.hireDate(),
                matchingPresence.getPresenceNumber(),
                matchingPresence.getCompanyCode(),
                matchingPresence.getEntryReasonCode(),
                matchingLaborClassification.getAgreementCode(),
                matchingLaborClassification.getAgreementCategoryCode(),
                matchingContract.getContractCode(),
                matchingContract.getContractSubtypeCode(),
                matchingWorkCenter.getWorkCenterAssignmentNumber(),
                matchingWorkCenter.getWorkCenterCode(),
                false
        );
    }

    private Presence requireSingleActivePresence(LocalDate hireDate, List<Presence> presences) {
        List<Presence> activePresences = presences.stream().filter(Presence::isActive).toList();
        if (activePresences.size() != 1) {
            throw new HireEmployeeConflictException("existing presence state is inconsistent for idempotent hire retry");
        }

        Presence presence = activePresences.get(0);
        if (!hireDate.equals(presence.getStartDate())) {
            throw new HireEmployeeConflictException("existing initial presence startDate is not equivalent to hireDate");
        }

        return presence;
    }

    private LaborClassification requireSingleActiveLaborClassification(
            LocalDate hireDate,
            List<LaborClassification> laborClassifications
    ) {
        List<LaborClassification> activeLaborClassifications = laborClassifications.stream()
                .filter(LaborClassification::isActive)
                .toList();
        if (activeLaborClassifications.size() != 1) {
            throw new HireEmployeeConflictException(
                    "existing labor classification state is inconsistent for idempotent hire retry"
            );
        }

        LaborClassification laborClassification = activeLaborClassifications.get(0);
        if (!hireDate.equals(laborClassification.getStartDate())) {
            throw new HireEmployeeConflictException("existing initial laborClassification startDate is not equivalent to hireDate");
        }

        return laborClassification;
    }

    private Contract requireSingleActiveContract(LocalDate hireDate, List<Contract> contracts) {
        List<Contract> activeContracts = contracts.stream().filter(Contract::isActive).toList();
        if (activeContracts.size() != 1) {
            throw new HireEmployeeConflictException("existing contract state is inconsistent for idempotent hire retry");
        }

        Contract contract = activeContracts.get(0);
        if (!hireDate.equals(contract.getStartDate())) {
            throw new HireEmployeeConflictException("existing initial contract startDate is not equivalent to hireDate");
        }

        return contract;
    }

    private WorkCenter requireSingleActiveWorkCenter(LocalDate hireDate, List<WorkCenter> workCenters) {
        List<WorkCenter> activeWorkCenters = workCenters.stream().filter(WorkCenter::isActive).toList();
        if (activeWorkCenters.size() != 1) {
            throw new HireEmployeeConflictException("existing work center state is inconsistent for idempotent hire retry");
        }

        WorkCenter workCenter = activeWorkCenters.get(0);
        if (!hireDate.equals(workCenter.getStartDate())) {
            throw new HireEmployeeConflictException("existing initial workCenter startDate is not equivalent to hireDate");
        }

        return workCenter;
    }

    private void requireEquals(String actual, String expected, String fieldName) {
        if (!Objects.equals(normalizeNullable(actual), normalizeNullable(expected))) {
            throw new HireEmployeeConflictException("existing employee " + fieldName + " is not equivalent");
        }
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}