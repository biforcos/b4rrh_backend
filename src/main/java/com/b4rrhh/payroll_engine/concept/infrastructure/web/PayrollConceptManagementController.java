package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import com.b4rrhh.payroll_engine.concept.application.usecase.CreatePayrollConceptUseCase;
import com.b4rrhh.payroll_engine.concept.application.usecase.DeletePayrollConceptUseCase;
import com.b4rrhh.payroll_engine.concept.domain.port.PayrollConceptRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payroll-engine/{ruleSystemCode}/concepts")
public class PayrollConceptManagementController {

    private final CreatePayrollConceptUseCase createPayrollConceptUseCase;
    private final DeletePayrollConceptUseCase deletePayrollConceptUseCase;
    private final PayrollConceptRepository payrollConceptRepository;
    private final PayrollConceptManagementAssembler assembler;

    public PayrollConceptManagementController(
            CreatePayrollConceptUseCase createPayrollConceptUseCase,
            DeletePayrollConceptUseCase deletePayrollConceptUseCase,
            PayrollConceptRepository payrollConceptRepository,
            PayrollConceptManagementAssembler assembler
    ) {
        this.createPayrollConceptUseCase = createPayrollConceptUseCase;
        this.deletePayrollConceptUseCase = deletePayrollConceptUseCase;
        this.payrollConceptRepository = payrollConceptRepository;
        this.assembler = assembler;
    }

    @GetMapping
    public List<PayrollConceptDesignerResponse> list(@PathVariable String ruleSystemCode) {
        return payrollConceptRepository.findAllByRuleSystemCode(ruleSystemCode)
                .stream()
                .map(assembler::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollConceptDesignerResponse create(
            @PathVariable String ruleSystemCode,
            @RequestBody CreatePayrollConceptRequest request
    ) {
        return assembler.toResponse(
                createPayrollConceptUseCase.create(assembler.toCommand(ruleSystemCode, request))
        );
    }

    @DeleteMapping("/{conceptCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String conceptCode
    ) {
        deletePayrollConceptUseCase.delete(ruleSystemCode, conceptCode);
    }
}
