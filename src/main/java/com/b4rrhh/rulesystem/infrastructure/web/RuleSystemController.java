package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.CreateRuleSystemCommand;
import com.b4rrhh.rulesystem.application.usecase.CreateRuleSystemUseCase;
import com.b4rrhh.rulesystem.application.usecase.GetRuleSystemByCodeUseCase;
import com.b4rrhh.rulesystem.application.usecase.ListRuleSystemsUseCase;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.infrastructure.web.dto.CreateRuleSystemRequest;
import com.b4rrhh.rulesystem.infrastructure.web.dto.RuleSystemResponse;
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

    public RuleSystemController(
            CreateRuleSystemUseCase createRuleSystemUseCase,
            GetRuleSystemByCodeUseCase getRuleSystemByCodeUseCase,
            ListRuleSystemsUseCase listRuleSystemsUseCase
    ) {
        this.createRuleSystemUseCase = createRuleSystemUseCase;
        this.getRuleSystemByCodeUseCase = getRuleSystemByCodeUseCase;
        this.listRuleSystemsUseCase = listRuleSystemsUseCase;
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

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping("/{code}")
    public ResponseEntity<RuleSystemResponse> getByCode(@PathVariable String code) {
        return getRuleSystemByCodeUseCase.getByCode(code)
                .map(ruleSystem -> ResponseEntity.ok(toResponse(ruleSystem)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<RuleSystemResponse>> list() {
        List<RuleSystemResponse> response = listRuleSystemsUseCase.listAll()
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    private RuleSystemResponse toResponse(RuleSystem ruleSystem) {
        return new RuleSystemResponse(
                ruleSystem.getId(),
                ruleSystem.getCode(),
                ruleSystem.getName(),
                ruleSystem.getCountryCode(),
                ruleSystem.isActive()
        );
    }
}