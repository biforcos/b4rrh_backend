package com.b4rrhh.employee.tax_information.domain.port;

import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EmployeeTaxInformationRepository {
    EmployeeTaxInformation save(EmployeeTaxInformation taxInformation);
    Optional<EmployeeTaxInformation> findByEmployeeIdAndValidFrom(Long employeeId, LocalDate validFrom);
    List<EmployeeTaxInformation> findAllByEmployeeIdOrderByValidFromDesc(Long employeeId);
    Optional<EmployeeTaxInformation> findLatestOnOrBefore(Long employeeId, LocalDate referenceDate);
    void deleteByEmployeeIdAndValidFrom(Long employeeId, LocalDate validFrom);
}
