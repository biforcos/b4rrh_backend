package com.b4rrhh.employee.employee.domain.port;

import com.b4rrhh.employee.employee.domain.model.EmployeeDirectoryItem;

import java.util.List;

public interface EmployeeDirectoryRepository {

    List<EmployeeDirectoryItem> findDirectoryByFilters(
            String queryText,
            String ruleSystemCode,
            String employeeTypeCode,
            String status,
            int page,
            int size
    );
}