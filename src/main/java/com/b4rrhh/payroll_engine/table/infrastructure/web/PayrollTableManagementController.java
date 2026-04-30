package com.b4rrhh.payroll_engine.table.infrastructure.web;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObject;
import com.b4rrhh.payroll_engine.table.application.usecase.CreatePayrollTableCommand;
import com.b4rrhh.payroll_engine.table.application.usecase.CreatePayrollTableUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payroll-engine/{ruleSystemCode}/tables")
public class PayrollTableManagementController {

    private final CreatePayrollTableUseCase createPayrollTableUseCase;

    public PayrollTableManagementController(CreatePayrollTableUseCase createPayrollTableUseCase) {
        this.createPayrollTableUseCase = createPayrollTableUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PayrollTableResponse create(
            @PathVariable String ruleSystemCode,
            @Valid @RequestBody CreatePayrollTableRequest request
    ) {
        PayrollObject saved = createPayrollTableUseCase.create(
                new CreatePayrollTableCommand(ruleSystemCode, request.objectCode())
        );
        return new PayrollTableResponse(saved.getRuleSystemCode(), saved.getObjectCode());
    }
}
