package com.b4rrhh.rulesystem.workcenter.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataWorkCenterContactRepository extends JpaRepository<WorkCenterContactEntity, Long> {

    Optional<WorkCenterContactEntity> findTopByWorkCenterRuleEntityIdOrderByContactNumberDesc(Long workCenterRuleEntityId);

    List<WorkCenterContactEntity> findByWorkCenterRuleEntityIdOrderByContactNumberAsc(Long workCenterRuleEntityId);

    Optional<WorkCenterContactEntity> findByWorkCenterRuleEntityIdAndContactNumber(
            Long workCenterRuleEntityId,
            Integer contactNumber
    );

    void deleteByWorkCenterRuleEntityIdAndContactNumber(Long workCenterRuleEntityId, Integer contactNumber);
}