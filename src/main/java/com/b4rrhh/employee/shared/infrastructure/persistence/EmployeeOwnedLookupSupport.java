package com.b4rrhh.employee.shared.infrastructure.persistence;

import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

@Component
public class EmployeeOwnedLookupSupport {

    private final EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport;

    public EmployeeOwnedLookupSupport(EmployeeBusinessKeyLookupSupport employeeBusinessKeyLookupSupport) {
        this.employeeBusinessKeyLookupSupport = employeeBusinessKeyLookupSupport;
    }

    public <T> Optional<T> findOwnedByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Function<EmployeeEntity, Optional<T>> ownedLookup
    ) {
        return employeeBusinessKeyLookupSupport
                .findByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .flatMap(ownedLookup);
    }

    public <T> Optional<T> findOwnedByBusinessKeyForUpdate(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Function<EmployeeEntity, Optional<T>> ownedLookup
    ) {
        return employeeBusinessKeyLookupSupport
                .findByBusinessKeyForUpdate(ruleSystemCode, employeeTypeCode, employeeNumber)
                .flatMap(ownedLookup);
    }

    public <T> T findOwnedByBusinessKeyOrThrow(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Function<EmployeeEntity, Optional<T>> ownedLookup,
            Supplier<? extends RuntimeException> employeeNotFoundExceptionSupplier,
            Supplier<? extends RuntimeException> ownedNotFoundExceptionSupplier
    ) {
        EmployeeEntity employee = employeeBusinessKeyLookupSupport
                .findByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .orElseThrow(employeeNotFoundExceptionSupplier);

        return ownedLookup.apply(employee).orElseThrow(ownedNotFoundExceptionSupplier);
    }

    public <T> T findOwnedByBusinessKeyForUpdateOrThrow(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            Function<EmployeeEntity, Optional<T>> ownedLookup,
            Supplier<? extends RuntimeException> employeeNotFoundExceptionSupplier,
            Supplier<? extends RuntimeException> ownedNotFoundExceptionSupplier
    ) {
        EmployeeEntity employee = employeeBusinessKeyLookupSupport
                .findByBusinessKeyForUpdate(ruleSystemCode, employeeTypeCode, employeeNumber)
                .orElseThrow(employeeNotFoundExceptionSupplier);

        return ownedLookup.apply(employee).orElseThrow(ownedNotFoundExceptionSupplier);
    }
}