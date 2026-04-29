package com.b4rrhh.employee.payroll_input.infrastructure.web.assembler;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputResponse;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputsResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EmployeePayrollInputResponseAssembler {

    public EmployeePayrollInputResponse toResponse(EmployeePayrollInput input) {
        return new EmployeePayrollInputResponse(
                input.getConceptCode(),
                input.getPeriod(),
                input.getQuantity()
        );
    }

    public EmployeePayrollInputsResponse toListResponse(int period, List<EmployeePayrollInput> inputs) {
        List<EmployeePayrollInputResponse> items = inputs.stream()
                .map(this::toResponse)
                .toList();
        return new EmployeePayrollInputsResponse(period, items);
    }
}
