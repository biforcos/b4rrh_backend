package com.b4rrhh.employee.cost_center.infrastructure.persistence;

import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import com.b4rrhh.employee.cost_center.domain.port.CostCenterRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class CostCenterPersistenceAdapter implements CostCenterRepository {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final SpringDataCostCenterRepository springDataCostCenterRepository;

    public CostCenterPersistenceAdapter(SpringDataCostCenterRepository springDataCostCenterRepository) {
        this.springDataCostCenterRepository = springDataCostCenterRepository;
    }

    @Override
    public Optional<CostCenterAllocation> findByEmployeeIdAndCostCenterCodeAndStartDate(
            Long employeeId,
            String costCenterCode,
            LocalDate startDate
    ) {
        return springDataCostCenterRepository
                .findByEmployeeIdAndCostCenterCodeAndStartDate(employeeId, costCenterCode, startDate)
                .map(this::toDomain);
    }

    @Override
    public List<CostCenterAllocation> findByEmployeeIdOrderByStartDate(Long employeeId) {
        return springDataCostCenterRepository
                .findByEmployeeIdOrderByStartDateAsc(employeeId)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existsOverlappingPeriodByCostCenterCode(
            Long employeeId,
            String costCenterCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate effectiveEndDate = endDate == null ? MAX_DATE : endDate;
        return springDataCostCenterRepository.existsOverlappingPeriodByCostCenterCode(
                employeeId,
                costCenterCode,
                startDate,
                effectiveEndDate,
                MAX_DATE
        );
    }

    @Override
    public void save(CostCenterAllocation costCenterAllocation) {
        springDataCostCenterRepository.save(toEntity(costCenterAllocation));
    }

    @Override
    public void update(CostCenterAllocation costCenterAllocation) {
        CostCenterEntity entity = springDataCostCenterRepository
                .findByEmployeeIdAndCostCenterCodeAndStartDate(
                        costCenterAllocation.getEmployeeId(),
                        costCenterAllocation.getCostCenterCode(),
                        costCenterAllocation.getStartDate()
                )
                .orElseThrow(() -> new IllegalStateException(
                        "Cost center allocation not found for update by functional identity"
                ));

        entity.setAllocationPercentage(costCenterAllocation.getAllocationPercentage());
        entity.setEndDate(costCenterAllocation.getEndDate());
        springDataCostCenterRepository.save(entity);
    }

    private CostCenterAllocation toDomain(CostCenterEntity entity) {
        return new CostCenterAllocation(
                entity.getEmployeeId(),
                entity.getCostCenterCode(),
                entity.getAllocationPercentage(),
                entity.getStartDate(),
                entity.getEndDate()
        );
    }

    private CostCenterEntity toEntity(CostCenterAllocation costCenterAllocation) {
        CostCenterEntity entity = new CostCenterEntity();
        entity.setEmployeeId(costCenterAllocation.getEmployeeId());
        entity.setCostCenterCode(costCenterAllocation.getCostCenterCode());
        entity.setAllocationPercentage(costCenterAllocation.getAllocationPercentage());
        entity.setStartDate(costCenterAllocation.getStartDate());
        entity.setEndDate(costCenterAllocation.getEndDate());
        return entity;
    }
}
