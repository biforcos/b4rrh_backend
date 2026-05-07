package com.b4rrhh.employee.tax_information.infrastructure.web;

import com.b4rrhh.employee.tax_information.application.usecase.*;
import com.b4rrhh.employee.tax_information.infrastructure.web.assembler.EmployeeTaxInformationAssembler;
import com.b4rrhh.employee.tax_information.infrastructure.web.dto.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/tax-information")
public class EmployeeTaxInformationBusinessKeyController {

    private final CreateEmployeeTaxInformationUseCase createUseCase;
    private final CorrectEmployeeTaxInformationUseCase correctUseCase;
    private final DeleteEmployeeTaxInformationUseCase deleteUseCase;
    private final GetEmployeeTaxInformationUseCase getUseCase;
    private final ListEmployeeTaxInformationUseCase listUseCase;
    private final EmployeeTaxInformationAssembler assembler;

    public EmployeeTaxInformationBusinessKeyController(
            CreateEmployeeTaxInformationUseCase createUseCase,
            CorrectEmployeeTaxInformationUseCase correctUseCase,
            DeleteEmployeeTaxInformationUseCase deleteUseCase,
            GetEmployeeTaxInformationUseCase getUseCase,
            ListEmployeeTaxInformationUseCase listUseCase,
            EmployeeTaxInformationAssembler assembler) {
        this.createUseCase = createUseCase;
        this.correctUseCase = correctUseCase;
        this.deleteUseCase = deleteUseCase;
        this.getUseCase = getUseCase;
        this.listUseCase = listUseCase;
        this.assembler = assembler;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeTaxInformationResponse create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateEmployeeTaxInformationRequest request) {
        return assembler.toResponse(createUseCase.create(
            assembler.toCreateCommand(ruleSystemCode, employeeTypeCode, employeeNumber, request)));
    }

    @GetMapping
    public List<EmployeeTaxInformationResponse> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber) {
        return assembler.toResponseList(listUseCase.list(
            new ListEmployeeTaxInformationCommand(ruleSystemCode, employeeTypeCode, employeeNumber)));
    }

    @GetMapping("/{validFrom}")
    public EmployeeTaxInformationResponse get(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom) {
        return assembler.toResponse(getUseCase.get(
            new GetEmployeeTaxInformationCommand(ruleSystemCode, employeeTypeCode, employeeNumber, validFrom)));
    }

    @PutMapping("/{validFrom}")
    public EmployeeTaxInformationResponse correct(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom,
            @RequestBody CorrectEmployeeTaxInformationRequest request) {
        return assembler.toResponse(correctUseCase.correct(
            assembler.toCorrectCommand(ruleSystemCode, employeeTypeCode, employeeNumber, validFrom, request)));
    }

    @DeleteMapping("/{validFrom}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate validFrom) {
        deleteUseCase.delete(
            new DeleteEmployeeTaxInformationCommand(ruleSystemCode, employeeTypeCode, employeeNumber, validFrom));
    }
}
