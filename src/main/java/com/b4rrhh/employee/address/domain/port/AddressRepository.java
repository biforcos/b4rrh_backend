package com.b4rrhh.employee.address.domain.port;

import com.b4rrhh.employee.address.domain.model.Address;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AddressRepository {

    Optional<Address> findByEmployeeIdAndAddressNumber(Long employeeId, Integer addressNumber);

    List<Address> findByEmployeeIdOrderByStartDate(Long employeeId);

        boolean existsOverlappingPeriodByAddressType(
            Long employeeId,
            String addressTypeCode,
            LocalDate startDate,
            LocalDate endDate
        );

    Optional<Integer> findMaxAddressNumberByEmployeeId(Long employeeId);

    Address save(Address address);
}
