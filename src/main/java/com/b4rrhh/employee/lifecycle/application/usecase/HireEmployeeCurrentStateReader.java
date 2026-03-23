package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.labor_classification.application.command.ListEmployeeLaborClassificationsCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.ListEmployeeLaborClassificationsUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import org.springframework.stereotype.Component;

@Component
public class HireEmployeeCurrentStateReader {

    private final ListEmployeePresencesUseCase listEmployeePresencesUseCase;
    private final ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase;
    private final ListEmployeeContractsUseCase listEmployeeContractsUseCase;
    private final ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase;

    public HireEmployeeCurrentStateReader(
            ListEmployeePresencesUseCase listEmployeePresencesUseCase,
            ListEmployeeLaborClassificationsUseCase listEmployeeLaborClassificationsUseCase,
            ListEmployeeContractsUseCase listEmployeeContractsUseCase,
            ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase
    ) {
        this.listEmployeePresencesUseCase = listEmployeePresencesUseCase;
        this.listEmployeeLaborClassificationsUseCase = listEmployeeLaborClassificationsUseCase;
        this.listEmployeeContractsUseCase = listEmployeeContractsUseCase;
        this.listEmployeeWorkCentersUseCase = listEmployeeWorkCentersUseCase;
    }

    public HireEmployeeCurrentState read(
            Employee employee,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    ) {
        return new HireEmployeeCurrentState(
                employee,
                listEmployeePresencesUseCase.listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber),
                listEmployeeLaborClassificationsUseCase.listByEmployeeBusinessKey(new ListEmployeeLaborClassificationsCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                )),
                listEmployeeContractsUseCase.listByEmployeeBusinessKey(new ListEmployeeContractsCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber
                )),
                listEmployeeWorkCentersUseCase.listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
        );
    }
}