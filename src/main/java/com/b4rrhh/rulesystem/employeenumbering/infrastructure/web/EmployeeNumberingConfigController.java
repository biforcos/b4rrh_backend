package com.b4rrhh.rulesystem.employeenumbering.infrastructure.web;

import com.b4rrhh.rulesystem.employeenumbering.application.usecase.GetEmployeeNumberingConfigUseCase;
import com.b4rrhh.rulesystem.employeenumbering.application.usecase.UpsertEmployeeNumberingConfigCommand;
import com.b4rrhh.rulesystem.employeenumbering.application.usecase.UpsertEmployeeNumberingConfigUseCase;
import com.b4rrhh.rulesystem.employeenumbering.domain.model.EmployeeNumberingConfig;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto.EmployeeNumberingConfigResponse;
import com.b4rrhh.rulesystem.employeenumbering.infrastructure.web.dto.UpsertEmployeeNumberingConfigRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rule-systems/{ruleSystemCode}/employee-numbering-config")
public class EmployeeNumberingConfigController {

    private final GetEmployeeNumberingConfigUseCase getUseCase;
    private final UpsertEmployeeNumberingConfigUseCase upsertUseCase;

    public EmployeeNumberingConfigController(
            GetEmployeeNumberingConfigUseCase getUseCase,
            UpsertEmployeeNumberingConfigUseCase upsertUseCase) {
        this.getUseCase = getUseCase;
        this.upsertUseCase = upsertUseCase;
    }

    @GetMapping
    public ResponseEntity<EmployeeNumberingConfigResponse> get(
            @PathVariable String ruleSystemCode) {
        return getUseCase.getByRuleSystemCode(ruleSystemCode)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<EmployeeNumberingConfigResponse> upsert(
            @PathVariable String ruleSystemCode,
            @RequestBody UpsertEmployeeNumberingConfigRequest request) {
        EmployeeNumberingConfig config = upsertUseCase.upsert(new UpsertEmployeeNumberingConfigCommand(
                ruleSystemCode,
                request.prefix() != null ? request.prefix() : "",
                request.numericPartLength(),
                request.step(),
                request.nextValue()
        ));
        return ResponseEntity.ok(toResponse(config));
    }

    private EmployeeNumberingConfigResponse toResponse(EmployeeNumberingConfig config) {
        return new EmployeeNumberingConfigResponse(
                config.ruleSystemCode(),
                config.prefix(),
                config.numericPartLength(),
                config.step(),
                config.nextValue(),
                config.formatNumber()
        );
    }
}
