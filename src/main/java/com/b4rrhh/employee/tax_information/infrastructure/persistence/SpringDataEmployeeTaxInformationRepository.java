package com.b4rrhh.employee.tax_information.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SpringDataEmployeeTaxInformationRepository
        extends JpaRepository<EmployeeTaxInformationEntity, Long> {

    List<EmployeeTaxInformationEntity> findByEmployeeIdOrderByValidFromDesc(Long employeeId);

    Optional<EmployeeTaxInformationEntity> findByEmployeeIdAndValidFrom(Long employeeId, LocalDate validFrom);

    Optional<EmployeeTaxInformationEntity> findFirstByEmployeeIdAndValidFromLessThanEqualOrderByValidFromDesc(
            Long employeeId, LocalDate date);

    void deleteByEmployeeIdAndValidFrom(Long employeeId, LocalDate validFrom);
}
