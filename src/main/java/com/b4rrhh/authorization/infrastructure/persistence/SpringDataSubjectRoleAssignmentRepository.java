package com.b4rrhh.authorization.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SpringDataSubjectRoleAssignmentRepository extends JpaRepository<SubjectRoleAssignmentEntity, SubjectRoleAssignmentEntity.Pk> {

    List<SubjectRoleAssignmentEntity> findBySubjectCodeAndActiveTrue(String subjectCode);
}