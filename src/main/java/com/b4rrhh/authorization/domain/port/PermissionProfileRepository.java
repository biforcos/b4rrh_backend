package com.b4rrhh.authorization.domain.port;

import com.b4rrhh.authorization.domain.model.PermissionProfile;

import java.util.Optional;

public interface PermissionProfileRepository {

    Optional<PermissionProfile> findByCode(String code);
}
