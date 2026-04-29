package com.b4rrhh.employee.payroll_input.infrastructure.web;

import com.b4rrhh.employee.payroll_input.application.usecase.CreateEmployeePayrollInputCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.CreateEmployeePayrollInputUseCase;
import com.b4rrhh.employee.payroll_input.application.usecase.DeleteEmployeePayrollInputCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.DeleteEmployeePayrollInputUseCase;
import com.b4rrhh.employee.payroll_input.application.usecase.ListEmployeePayrollInputsCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.ListEmployeePayrollInputsUseCase;
import com.b4rrhh.employee.payroll_input.application.usecase.UpdateEmployeePayrollInputCommand;
import com.b4rrhh.employee.payroll_input.application.usecase.UpdateEmployeePayrollInputUseCase;
import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.infrastructure.web.assembler.EmployeePayrollInputResponseAssembler;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.CreateEmployeePayrollInputRequest;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputResponse;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.EmployeePayrollInputsResponse;
import com.b4rrhh.employee.payroll_input.infrastructure.web.dto.UpdateEmployeePayrollInputRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/payroll-inputs")
public class EmployeePayrollInputController {

    private final CreateEmployeePayrollInputUseCase createUseCase;
    private final UpdateEmployeePayrollInputUseCase updateUseCase;
    private final DeleteEmployeePayrollInputUseCase deleteUseCase;
    private final ListEmployeePayrollInputsUseCase listUseCase;
    private final EmployeePayrollInputResponseAssembler assembler;

    public EmployeePayrollInputController(
            CreateEmployeePayrollInputUseCase createUseCase,
            UpdateEmployeePayrollInputUseCase updateUseCase,
            DeleteEmployeePayrollInputUseCase deleteUseCase,
            ListEmployeePayrollInputsUseCase listUseCase,
            EmployeePayrollInputResponseAssembler assembler
    ) {
        this.createUseCase = createUseCase;
        this.updateUseCase = updateUseCase;
        this.deleteUseCase = deleteUseCase;
        this.listUseCase = listUseCase;
        this.assembler = assembler;
    }

    @PostMapping
    public ResponseEntity<EmployeePayrollInputResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateEmployeePayrollInputRequest request
    ) {
        EmployeePayrollInput created = createUseCase.create(new CreateEmployeePayrollInputCommand(
                ruleSystemCode, employeeTypeCode, employeeNumber,
                request.getConceptCode(), request.getPeriod(), request.getQuantity()
        ));
        return ResponseEntity.status(HttpStatus.CREATED).body(assembler.toResponse(created));
    }

    @GetMapping
    public ResponseEntity<EmployeePayrollInputsResponse> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestParam int period
    ) {
        List<EmployeePayrollInput> inputs = listUseCase.listByEmployeeAndPeriod(
                new ListEmployeePayrollInputsCommand(ruleSystemCode, employeeTypeCode, employeeNumber, period));
        return ResponseEntity.ok(assembler.toListResponse(period, inputs));
    }

    @PutMapping("/{conceptCode}")
    public ResponseEntity<EmployeePayrollInputResponse> update(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String conceptCode,
            @RequestParam int period,
            @RequestBody UpdateEmployeePayrollInputRequest request
    ) {
        EmployeePayrollInput updated = updateUseCase.update(new UpdateEmployeePayrollInputCommand(
                ruleSystemCode, employeeTypeCode, employeeNumber,
                conceptCode, period, request.getQuantity()
        ));
        return ResponseEntity.ok(assembler.toResponse(updated));
    }

    @DeleteMapping("/{conceptCode}")
    public ResponseEntity<Void> delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String conceptCode,
            @RequestParam int period
    ) {
        deleteUseCase.delete(new DeleteEmployeePayrollInputCommand(
                ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period));
        return ResponseEntity.noContent().build();
    }
}
