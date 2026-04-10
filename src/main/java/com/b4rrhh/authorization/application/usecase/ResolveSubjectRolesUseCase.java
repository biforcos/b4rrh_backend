package com.b4rrhh.authorization.application.usecase;

import java.util.List;

public interface ResolveSubjectRolesUseCase {

    List<String> resolveActiveRoleCodes(String subjectCode);
}