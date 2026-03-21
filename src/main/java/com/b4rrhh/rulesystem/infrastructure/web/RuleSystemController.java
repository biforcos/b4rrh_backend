package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.CreateRuleSystemCommand;
import com.b4rrhh.rulesystem.application.usecase.CreateRuleSystemUseCase;
import com.b4rrhh.rulesystem.application.usecase.GetRuleSystemByCodeUseCase;
import com.b4rrhh.rulesystem.application.usecase.ListRuleSystemsUseCase;
import com.b4rrhh.rulesystem.application.usecase.UpdateRuleSystemUseCase;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.infrastructure.web.dto.CreateRuleSystemRequest;
import com.b4rrhh.rulesystem.infrastructure.web.dto.RuleSystemResponse;
import com.b4rrhh.rulesystem.infrastructure.web.dto.UpdateRuleSystemRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/rule-systems")
public class RuleSystemController {

    private final CreateRuleSystemUseCase createRuleSystemUseCase;
    private final GetRuleSystemByCodeUseCase getRuleSystemByCodeUseCase;
    private final ListRuleSystemsUseCase listRuleSystemsUseCase;
    private final UpdateRuleSystemUseCase updateRuleSystemUseCase;
    private final RuleSystemMapper mapper;

    public RuleSystemController(
            CreateRuleSystemUseCase createRuleSystemUseCase,
            GetRuleSystemByCodeUseCase getRuleSystemByCodeUseCase,
            ListRuleSystemsUseCase listRuleSystemsUseCase,
            UpdateRuleSystemUseCase updateRuleSystemUseCase
    ) {
        this.createRuleSystemUseCase = createRuleSystemUseCase;
        this.getRuleSystemByCodeUseCase = getRuleSystemByCodeUseCase;
        this.listRuleSystemsUseCase = listRuleSystemsUseCase;
        this.updateRuleSystemUseCase = updateRuleSystemUseCase;
        this.mapper = new RuleSystemMapper();
    }

    @PostMapping
    public ResponseEntity<RuleSystemResponse> create(@RequestBody CreateRuleSystemRequest request) {
        RuleSystem created = createRuleSystemUseCase.create(
                new CreateRuleSystemCommand(
                        request.code(),
                        request.name(),
                        request.countryCode()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    @GetMapping("/{ruleSystemCode}")
    public ResponseEntity<RuleSystemResponse> getByCode(@PathVariable String ruleSystemCode) {
        return getRuleSystemByCodeUseCase.getByCode(ruleSystemCode)
                .map(ruleSystem -> ResponseEntity.ok(mapper.toResponse(ruleSystem)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{ruleSystemCode}")
    public ResponseEntity<RuleSystemResponse> updateRuleSystem(
            @PathVariable String ruleSystemCode,
            @RequestBody @Valid UpdateRuleSystemRequest request
    ) {
        RuleSystem updated = updateRuleSystemUseCase.execute(
                mapper.toUpdateCommand(ruleSystemCode, request)
        );

        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @GetMapping
    public ResponseEntity<List<RuleSystemResponse>> list() {
        List<RuleSystemResponse> response = listRuleSystemsUseCase.listAll()
                .stream()
                .map(mapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }
}