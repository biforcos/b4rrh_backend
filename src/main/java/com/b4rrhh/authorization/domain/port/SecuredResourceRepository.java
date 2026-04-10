package com.b4rrhh.authorization.domain.port;

import com.b4rrhh.authorization.domain.model.SecuredResource;

import java.util.Optional;

public interface SecuredResourceRepository {

    Optional<SecuredResource> findByCode(String code);
}
