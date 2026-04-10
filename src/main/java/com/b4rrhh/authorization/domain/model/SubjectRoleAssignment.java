package com.b4rrhh.authorization.domain.model;

public class SubjectRoleAssignment {

    private final String subjectCode;
    private final String roleCode;
    private final boolean active;
    private final SubjectRoleAssignmentOrigin assignmentOrigin;

    public SubjectRoleAssignment(
            String subjectCode,
            String roleCode,
            boolean active,
            SubjectRoleAssignmentOrigin assignmentOrigin
    ) {
        this.subjectCode = subjectCode;
        this.roleCode = roleCode;
        this.active = active;
        this.assignmentOrigin = assignmentOrigin;
    }

    public String subjectCode() { return subjectCode; }
    public String roleCode() { return roleCode; }
    public boolean active() { return active; }
    public SubjectRoleAssignmentOrigin assignmentOrigin() { return assignmentOrigin; }
}