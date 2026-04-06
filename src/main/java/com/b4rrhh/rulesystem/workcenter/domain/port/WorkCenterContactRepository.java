package com.b4rrhh.rulesystem.workcenter.domain.port;

import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterContact;

import java.util.List;
import java.util.Optional;

public interface WorkCenterContactRepository {

    Integer nextContactNumberForWorkCenterRuleEntityId(Long workCenterRuleEntityId);

    List<WorkCenterContact> findByWorkCenterRuleEntityIdOrderByContactNumberAsc(Long workCenterRuleEntityId);

    Optional<WorkCenterContact> findByWorkCenterRuleEntityIdAndContactNumber(Long workCenterRuleEntityId, Integer contactNumber);

    WorkCenterContact save(Long workCenterRuleEntityId, WorkCenterContact contact);

    void deleteByWorkCenterRuleEntityIdAndContactNumber(Long workCenterRuleEntityId, Integer contactNumber);
}