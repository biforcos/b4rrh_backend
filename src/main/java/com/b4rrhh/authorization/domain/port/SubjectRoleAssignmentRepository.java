package com.b4rrhh.authorization.domain.port;

import java.util.List;

public interface SubjectRoleAssignmentRepository {

    List<String> findActiveRoleCodesBySubjectCode(String subjectCode);
}