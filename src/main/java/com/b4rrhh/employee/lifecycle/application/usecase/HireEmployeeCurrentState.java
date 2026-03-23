package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;

import java.util.List;

public record HireEmployeeCurrentState(
        Employee employee,
        List<Presence> presences,
        List<LaborClassification> laborClassifications,
        List<Contract> contracts,
        List<WorkCenter> workCenters
) {
}