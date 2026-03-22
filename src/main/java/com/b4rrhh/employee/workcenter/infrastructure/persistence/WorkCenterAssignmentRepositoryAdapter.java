package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterAssignmentRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class WorkCenterAssignmentRepositoryAdapter implements WorkCenterAssignmentRepository {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final SpringDataWorkCenterRepository springDataWorkCenterRepository;
    private final WorkCenterAssignmentEntityMapper mapper;

    public WorkCenterAssignmentRepositoryAdapter(
            SpringDataWorkCenterRepository springDataWorkCenterRepository,
            WorkCenterAssignmentEntityMapper mapper
    ) {
        this.springDataWorkCenterRepository = springDataWorkCenterRepository;
        this.mapper = mapper;
    }

    @Override
    public Optional<WorkCenterAssignment> findByEmployeeIdAndWorkCenterAssignmentNumber(Long employeeId, Integer workCenterAssignmentNumber) {
        return springDataWorkCenterRepository
                .findByEmployeeIdAndWorkCenterAssignmentNumber(employeeId, workCenterAssignmentNumber)
                .map(mapper::toDomain);
    }

    @Override
    public List<WorkCenterAssignment> findByEmployeeIdOrderByStartDate(Long employeeId) {
        return springDataWorkCenterRepository.findByEmployeeIdOrderByStartDateAsc(employeeId)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    public boolean existsOverlappingPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        LocalDate effectiveEndDate = endDate == null ? MAX_DATE : endDate;
        return springDataWorkCenterRepository.existsOverlappingPeriod(employeeId, startDate, effectiveEndDate, MAX_DATE);
    }

    @Override
    public boolean existsOverlappingPeriodExcludingAssignment(
            Long employeeId,
            Integer excludedWorkCenterAssignmentNumber,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate effectiveEndDate = endDate == null ? MAX_DATE : endDate;
        return springDataWorkCenterRepository.existsOverlappingPeriodExcludingAssignment(
                employeeId,
                excludedWorkCenterAssignmentNumber,
                startDate,
                effectiveEndDate,
                MAX_DATE
        );
    }

    @Override
    public Optional<Integer> findMaxWorkCenterAssignmentNumberByEmployeeId(Long employeeId) {
        return Optional.ofNullable(springDataWorkCenterRepository.findMaxWorkCenterAssignmentNumberByEmployeeId(employeeId));
    }

    @Override
    public WorkCenterAssignment save(WorkCenterAssignment workCenterAssignment) {
        WorkCenterEntity saved = springDataWorkCenterRepository.save(mapper.toEntity(workCenterAssignment));
        return mapper.toDomain(saved);
    }
}
