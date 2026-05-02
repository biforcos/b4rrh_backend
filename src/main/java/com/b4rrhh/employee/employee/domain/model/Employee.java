package com.b4rrhh.employee.employee.domain.model;

import java.time.LocalDateTime;

public class Employee {

    private final Long id;
    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final String firstName;
    private final String lastName1;
    private final String lastName2;
    private final String preferredName;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String photoUrl;

    public Employee(
            Long id,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String firstName,
            String lastName1,
            String lastName2,
            String preferredName,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String photoUrl
    ) {
        this.id = id;
        this.ruleSystemCode = ruleSystemCode;
        this.employeeTypeCode = employeeTypeCode;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName1 = lastName1;
        this.lastName2 = lastName2;
        this.preferredName = preferredName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.photoUrl = photoUrl;
    }

    public Long getId() { return id; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName1() { return lastName1; }
    public String getLastName2() { return lastName2; }
    public String getPreferredName() { return preferredName; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getPhotoUrl() { return photoUrl; }

    public boolean isActive() {
        return EmployeeStatus.ACTIVE.matches(status);
    }

    public boolean isTerminated() {
        return EmployeeStatus.TERMINATED.matches(status);
    }

    public Employee withPhotoUrl(String photoUrl) {
        return new Employee(id, ruleSystemCode, employeeTypeCode, employeeNumber,
                firstName, lastName1, lastName2, preferredName, status,
                createdAt, updatedAt, photoUrl);
    }

    public Employee withoutPhotoUrl() {
        return withPhotoUrl(null);
    }

    public Employee activate() {
        return new Employee(id, ruleSystemCode, employeeTypeCode, employeeNumber,
                firstName, lastName1, lastName2, preferredName,
                EmployeeStatus.ACTIVE.name(), createdAt, LocalDateTime.now(), photoUrl);
    }

    public Employee updateIdentityFields(
            String firstName, String lastName1, String lastName2, String preferredName) {
        return new Employee(id, ruleSystemCode, employeeTypeCode, employeeNumber,
                firstName, lastName1, lastName2, preferredName, status,
                createdAt, LocalDateTime.now(), photoUrl);
    }
}
