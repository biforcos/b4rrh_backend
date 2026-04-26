package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.employee.address.infrastructure.persistence.AddressEntity;
import com.b4rrhh.employee.address.infrastructure.persistence.SpringDataAddressRepository;
import com.b4rrhh.employee.employee.infrastructure.persistence.EmployeeEntity;
import com.b4rrhh.employee.identifier.infrastructure.persistence.IdentifierEntity;
import com.b4rrhh.employee.identifier.infrastructure.persistence.SpringDataIdentifierRepository;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeBusinessKeyLookupSupport;
import com.b4rrhh.payroll.application.port.EmployeePersonalDataContext;
import com.b4rrhh.payroll.application.port.EmployeePersonalDataLookupPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class EmployeePersonalDataLookupAdapter implements EmployeePersonalDataLookupPort {

    private final EmployeeBusinessKeyLookupSupport employeeLookupSupport;
    private final SpringDataIdentifierRepository identifierRepository;
    private final SpringDataAddressRepository addressRepository;

    public EmployeePersonalDataLookupAdapter(
            EmployeeBusinessKeyLookupSupport employeeLookupSupport,
            SpringDataIdentifierRepository identifierRepository,
            SpringDataAddressRepository addressRepository
    ) {
        this.employeeLookupSupport = employeeLookupSupport;
        this.identifierRepository = identifierRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public Optional<EmployeePersonalDataContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            LocalDate referenceDate
    ) {
        return employeeLookupSupport
                .findByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .map(employee -> buildContext(employee, referenceDate));
    }

    private EmployeePersonalDataContext buildContext(EmployeeEntity employee, LocalDate referenceDate) {
        String fullName = buildFullName(employee);

        String nif = identifierRepository
                .findByEmployeeIdOrderByIdentifierTypeCodeAsc(employee.getId())
                .stream()
                .filter(IdentifierEntity::isPrimary)
                .findFirst()
                .map(i -> i.getIdentifierValue())
                .orElse(null);

        Optional<AddressEntity> activeAddress = addressRepository
                .findByEmployeeIdOrderByStartDateAsc(employee.getId())
                .stream()
                .filter(a -> !a.getStartDate().isAfter(referenceDate)
                        && (a.getEndDate() == null || !a.getEndDate().isBefore(referenceDate)))
                .reduce((first, second) -> second);

        return new EmployeePersonalDataContext(
                fullName,
                nif,
                activeAddress.map(AddressEntity::getStreet).orElse(null),
                activeAddress.map(AddressEntity::getCity).orElse(null),
                activeAddress.map(AddressEntity::getPostalCode).orElse(null)
        );
    }

    private String buildFullName(EmployeeEntity employee) {
        StringBuilder sb = new StringBuilder(employee.getFirstName());
        sb.append(' ').append(employee.getLastName1());
        if (employee.getLastName2() != null && !employee.getLastName2().isBlank()) {
            sb.append(' ').append(employee.getLastName2());
        }
        return sb.toString();
    }
}
