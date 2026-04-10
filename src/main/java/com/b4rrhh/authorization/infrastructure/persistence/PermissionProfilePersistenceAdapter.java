package com.b4rrhh.authorization.infrastructure.persistence;

import com.b4rrhh.authorization.domain.model.PermissionProfile;
import com.b4rrhh.authorization.domain.port.PermissionProfileRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class PermissionProfilePersistenceAdapter implements PermissionProfileRepository {

    private final SpringDataPermissionProfileRepository profileRepository;
    private final SpringDataPermissionProfileActionRepository actionRepository;

    public PermissionProfilePersistenceAdapter(
            SpringDataPermissionProfileRepository profileRepository,
            SpringDataPermissionProfileActionRepository actionRepository
    ) {
        this.profileRepository = profileRepository;
        this.actionRepository = actionRepository;
    }

    @Override
    public Optional<PermissionProfile> findByCode(String code) {
        return profileRepository.findByCodeAndActiveTrue(code).map(entity -> toDomain(entity, code));
    }

    private PermissionProfile toDomain(PermissionProfileEntity entity, String code) {
        Set<String> actionCodes = actionRepository.findByPermissionProfileCode(code)
                .stream()
                .map(PermissionProfileActionEntity::getActionCode)
                .collect(Collectors.toSet());

        return new PermissionProfile(entity.getCode(), entity.getName(), actionCodes);
    }
}
