package com.b4rrhh.employee.employee.domain.model;

public enum EmployeeStatus {
    ACTIVE,
    TERMINATED;

    public boolean matches(String value) {
        return this.name().equalsIgnoreCase(value);
    }
}
