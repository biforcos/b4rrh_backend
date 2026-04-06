package com.b4rrhh.rulesystem.workcenter.domain.port;

import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterProfile;

import java.util.Map;
import java.util.Optional;

public interface WorkCenterProfileRepository {

    Optional<WorkCenterProfile> findByWorkCenterRuleEntityId(Long workCenterRuleEntityId);

    Map<Long, WorkCenterProfile> findByWorkCenterRuleEntityIds(Iterable<Long> workCenterRuleEntityIds);

    WorkCenterProfile save(Long workCenterRuleEntityId, WorkCenterProfile workCenterProfile);
}