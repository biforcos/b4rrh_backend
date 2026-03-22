package com.b4rrhh.employee.workcenter.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.domain.model.WorkCenterAssignment;
import org.springframework.stereotype.Component;

@Component
public class WorkCenterAssignmentEntityMapper {

    public WorkCenterAssignment toDomain(WorkCenterEntity entity) {
        return new WorkCenterAssignment(
                entity.getId(),
                entity.getEmployeeId(),
                entity.getWorkCenterAssignmentNumber(),
                entity.getWorkCenterCode(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public WorkCenterEntity toEntity(WorkCenterAssignment workCenterAssignment) {
        WorkCenterEntity entity = new WorkCenterEntity();
        entity.setId(workCenterAssignment.getId());
        entity.setEmployeeId(workCenterAssignment.getEmployeeId());
        entity.setWorkCenterAssignmentNumber(workCenterAssignment.getWorkCenterAssignmentNumber());
        entity.setWorkCenterCode(workCenterAssignment.getWorkCenterCode());
        entity.setStartDate(workCenterAssignment.getStartDate());
        entity.setEndDate(workCenterAssignment.getEndDate());
        entity.setCreatedAt(workCenterAssignment.getCreatedAt());
        entity.setUpdatedAt(workCenterAssignment.getUpdatedAt());
        return entity;
    }
}
