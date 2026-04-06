package com.b4rrhh.rulesystem.workcenter.infrastructure.persistence;

import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterAddress;
import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterProfile;
import com.b4rrhh.rulesystem.workcenter.domain.port.WorkCenterProfileRepository;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class WorkCenterProfilePersistenceAdapter implements WorkCenterProfileRepository {

    private final SpringDataWorkCenterProfileRepository springDataWorkCenterProfileRepository;

    public WorkCenterProfilePersistenceAdapter(SpringDataWorkCenterProfileRepository springDataWorkCenterProfileRepository) {
        this.springDataWorkCenterProfileRepository = springDataWorkCenterProfileRepository;
    }

    @Override
    public Optional<WorkCenterProfile> findByWorkCenterRuleEntityId(Long workCenterRuleEntityId) {
        return springDataWorkCenterProfileRepository.findByWorkCenterRuleEntityId(workCenterRuleEntityId)
                .map(this::toDomain);
    }

    @Override
    public Map<Long, WorkCenterProfile> findByWorkCenterRuleEntityIds(Iterable<Long> workCenterRuleEntityIds) {
        java.util.List<Long> ids = new java.util.ArrayList<>();
        workCenterRuleEntityIds.forEach(ids::add);

        Map<Long, WorkCenterProfile> result = new HashMap<>();
        springDataWorkCenterProfileRepository.findByWorkCenterRuleEntityIdIn(ids)
                .forEach(entity -> result.put(entity.getWorkCenterRuleEntityId(), toDomain(entity)));
        return result;
    }

    @Override
    public WorkCenterProfile save(Long workCenterRuleEntityId, WorkCenterProfile workCenterProfile) {
        WorkCenterProfileEntity entity = springDataWorkCenterProfileRepository
                .findByWorkCenterRuleEntityId(workCenterRuleEntityId)
                .orElseGet(WorkCenterProfileEntity::new);

        entity.setWorkCenterRuleEntityId(workCenterRuleEntityId);
        entity.setCompanyCode(workCenterProfile.getCompanyCode());
        entity.setStreet(workCenterProfile.getAddress().getStreet());
        entity.setCity(workCenterProfile.getAddress().getCity());
        entity.setPostalCode(workCenterProfile.getAddress().getPostalCode());
        entity.setRegionCode(workCenterProfile.getAddress().getRegionCode());
        entity.setCountryCode(workCenterProfile.getAddress().getCountryCode());

        return toDomain(springDataWorkCenterProfileRepository.save(entity));
    }

    private WorkCenterProfile toDomain(WorkCenterProfileEntity entity) {
        return new WorkCenterProfile(
                entity.getCompanyCode(),
                new WorkCenterAddress(
                        entity.getStreet(),
                        entity.getCity(),
                        entity.getPostalCode(),
                        entity.getRegionCode(),
                        entity.getCountryCode()
                )
        );
    }
}