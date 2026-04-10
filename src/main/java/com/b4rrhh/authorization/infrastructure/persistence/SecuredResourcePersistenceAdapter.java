package com.b4rrhh.authorization.infrastructure.persistence;

import com.b4rrhh.authorization.domain.model.SecuredResource;
import com.b4rrhh.authorization.domain.model.SecuredResourceKind;
import com.b4rrhh.authorization.domain.port.SecuredResourceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SecuredResourcePersistenceAdapter implements SecuredResourceRepository {

    private final SpringDataSecuredResourceRepository springDataRepository;

    public SecuredResourcePersistenceAdapter(SpringDataSecuredResourceRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Optional<SecuredResource> findByCode(String code) {
        return springDataRepository.findByCode(code).map(this::toDomain);
    }

    private SecuredResource toDomain(SecuredResourceEntity entity) {
        SecuredResourceKind resourceKind;
        try {
            resourceKind = SecuredResourceKind.valueOf(entity.getResourceKind());
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Invalid resource_kind in database for secured resource '"
                    + entity.getCode() + "': " + entity.getResourceKind(), e
            );
        }

        return new SecuredResource(
                entity.getCode(),
                entity.getParentCode(),
                entity.getBoundedContextCode(),
                resourceKind,
                entity.getResourceFamilyCode(),
                entity.getName(),
                entity.isActive()
        );
    }
}
