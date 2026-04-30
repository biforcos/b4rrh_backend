package com.b4rrhh.payroll_engine.table.infrastructure.web;

import com.b4rrhh.payroll_engine.table.application.usecase.CreateTableRowCommand;
import com.b4rrhh.payroll_engine.table.application.usecase.CreateTableRowUseCase;
import com.b4rrhh.payroll_engine.table.application.usecase.DeleteTableRowUseCase;
import com.b4rrhh.payroll_engine.table.application.usecase.ListTableRowsUseCase;
import com.b4rrhh.payroll_engine.table.application.usecase.UpdateTableRowCommand;
import com.b4rrhh.payroll_engine.table.application.usecase.UpdateTableRowUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payroll-engine/{ruleSystemCode}/tables/{tableCode}/rows")
public class PayrollTableRowManagementController {

    private final ListTableRowsUseCase listTableRowsUseCase;
    private final CreateTableRowUseCase createTableRowUseCase;
    private final UpdateTableRowUseCase updateTableRowUseCase;
    private final DeleteTableRowUseCase deleteTableRowUseCase;

    public PayrollTableRowManagementController(
            ListTableRowsUseCase listTableRowsUseCase,
            CreateTableRowUseCase createTableRowUseCase,
            UpdateTableRowUseCase updateTableRowUseCase,
            DeleteTableRowUseCase deleteTableRowUseCase
    ) {
        this.listTableRowsUseCase = listTableRowsUseCase;
        this.createTableRowUseCase = createTableRowUseCase;
        this.updateTableRowUseCase = updateTableRowUseCase;
        this.deleteTableRowUseCase = deleteTableRowUseCase;
    }

    @GetMapping
    public List<TableRowResponse> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String tableCode
    ) {
        return listTableRowsUseCase.list(ruleSystemCode, tableCode)
                .stream()
                .map(TableRowResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TableRowResponse create(
            @PathVariable String ruleSystemCode,
            @PathVariable String tableCode,
            @Valid @RequestBody CreateTableRowRequest request
    ) {
        return TableRowResponse.from(createTableRowUseCase.create(new CreateTableRowCommand(
                ruleSystemCode, tableCode,
                request.searchCode(), request.startDate(), request.endDate(),
                request.monthlyValue(), request.annualValue(),
                request.dailyValue(), request.hourlyValue()
        )));
    }

    @PutMapping("/{rowId}")
    public TableRowResponse update(
            @PathVariable Long rowId,
            @RequestBody UpdateTableRowRequest request
    ) {
        return TableRowResponse.from(updateTableRowUseCase.update(new UpdateTableRowCommand(
                rowId, request.searchCode(), request.startDate(), request.endDate(),
                request.monthlyValue(), request.annualValue(),
                request.dailyValue(), request.hourlyValue(),
                request.active()
        )));
    }

    @DeleteMapping("/{rowId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long rowId) {
        deleteTableRowUseCase.delete(rowId);
    }
}
