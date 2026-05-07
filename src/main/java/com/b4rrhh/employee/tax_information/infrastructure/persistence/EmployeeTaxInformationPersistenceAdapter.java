package com.b4rrhh.employee.tax_information.infrastructure.persistence;

import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;
import com.b4rrhh.employee.tax_information.domain.port.EmployeeTaxInformationRepository;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Component
public class EmployeeTaxInformationPersistenceAdapter implements EmployeeTaxInformationRepository {

    private final SpringDataEmployeeTaxInformationRepository springDataRepo;

    public EmployeeTaxInformationPersistenceAdapter(SpringDataEmployeeTaxInformationRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public EmployeeTaxInformation save(EmployeeTaxInformation domain) {
        EmployeeTaxInformationEntity entity = toEntity(domain);
        if (entity.getCreatedAt() == null) entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        return toDomain(springDataRepo.save(entity));
    }

    @Override
    public Optional<EmployeeTaxInformation> findByEmployeeIdAndValidFrom(Long employeeId, LocalDate validFrom) {
        return springDataRepo.findByEmployeeIdAndValidFrom(employeeId, validFrom).map(this::toDomain);
    }

    @Override
    public List<EmployeeTaxInformation> findAllByEmployeeIdOrderByValidFromDesc(Long employeeId) {
        return springDataRepo.findByEmployeeIdOrderByValidFromDesc(employeeId)
                .stream().map(this::toDomain).toList();
    }

    @Override
    public Optional<EmployeeTaxInformation> findLatestOnOrBefore(Long employeeId, LocalDate referenceDate) {
        return springDataRepo
                .findFirstByEmployeeIdAndValidFromLessThanEqualOrderByValidFromDesc(employeeId, referenceDate)
                .map(this::toDomain);
    }

    @Override
    public void deleteByEmployeeIdAndValidFrom(Long employeeId, LocalDate validFrom) {
        springDataRepo.deleteByEmployeeIdAndValidFrom(employeeId, validFrom);
    }

    private EmployeeTaxInformation toDomain(EmployeeTaxInformationEntity e) {
        return EmployeeTaxInformation.rehydrate(
                e.getId(), e.getEmployeeId(), e.getValidFrom(),
                e.getFamilySituation(), e.getDescendantsCount(), e.getAscendantsCount(),
                e.getDisabilityDegree(), e.isPensionCompensatoria(), e.isGeographicMobility(),
                e.isHabitualResidenceLoan(), e.getTaxTerritory(),
                e.getCreatedAt(), e.getUpdatedAt());
    }

    private EmployeeTaxInformationEntity toEntity(EmployeeTaxInformation d) {
        EmployeeTaxInformationEntity e = new EmployeeTaxInformationEntity();
        e.setId(d.getId());
        e.setEmployeeId(d.getEmployeeId());
        e.setValidFrom(d.getValidFrom());
        e.setFamilySituation(d.getFamilySituation());
        e.setDescendantsCount(d.getDescendantsCount());
        e.setAscendantsCount(d.getAscendantsCount());
        e.setDisabilityDegree(d.getDisabilityDegree());
        e.setPensionCompensatoria(d.isPensionCompensatoria());
        e.setGeographicMobility(d.isGeographicMobility());
        e.setHabitualResidenceLoan(d.isHabitualResidenceLoan());
        e.setTaxTerritory(d.getTaxTerritory());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(d.getUpdatedAt());
        return e;
    }
}
