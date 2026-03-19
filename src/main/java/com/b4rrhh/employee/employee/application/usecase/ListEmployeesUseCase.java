package com.b4rrhh.employee.employee.application.usecase;

import com.b4rrhh.employee.employee.domain.model.EmployeeDirectoryItem;

import java.util.List;

public interface ListEmployeesUseCase {

    List<EmployeeDirectoryItem> list(ListEmployeesQuery query);
}