package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.CreateRuleEntityCommand;
import com.b4rrhh.rulesystem.application.usecase.CreateRuleEntityUseCase;
import com.b4rrhh.rulesystem.application.usecase.GetRuleEntityByIdUseCase;
import com.b4rrhh.rulesystem.application.usecase.ListRuleEntitiesQuery;
import com.b4rrhh.rulesystem.application.usecase.ListRuleEntitiesUseCase;
import com.b4rrhh.rulesystem.application.usecase.SearchRuleEntityByBusinessKeyUseCase;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.infrastructure.web.dto.CreateRuleEntityRequest;
import com.b4rrhh.rulesystem.infrastructure.web.dto.RuleEntityResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rule-entities")
public class RuleEntityController {

    private final CreateRuleEntityUseCase createRuleEntityUseCase;
    private final GetRuleEntityByIdUseCase getRuleEntityByIdUseCase;
    private final ListRuleEntitiesUseCase listRuleEntitiesUseCase;
    private final SearchRuleEntityByBusinessKeyUseCase searchRuleEntityByBusinessKeyUseCase;

    public RuleEntityController(
            CreateRuleEntityUseCase createRuleEntityUseCase,
            GetRuleEntityByIdUseCase getRuleEntityByIdUseCase,
            ListRuleEntitiesUseCase listRuleEntitiesUseCase,
            SearchRuleEntityByBusinessKeyUseCase searchRuleEntityByBusinessKeyUseCase
    ) {
        this.createRuleEntityUseCase = createRuleEntityUseCase;
        this.getRuleEntityByIdUseCase = getRuleEntityByIdUseCase;
        this.listRuleEntitiesUseCase = listRuleEntitiesUseCase;
        this.searchRuleEntityByBusinessKeyUseCase = searchRuleEntityByBusinessKeyUseCase;
    }

    @PostMapping
    public ResponseEntity<RuleEntityResponse> create(@RequestBody CreateRuleEntityRequest request) {
        RuleEntity created = createRuleEntityUseCase.create(
                new CreateRuleEntityCommand(
                        request.ruleSystemCode(),
                        request.ruleEntityTypeCode(),
                        request.code(),
                        request.name(),
                        request.description(),
                        request.startDate(),
                        request.endDate()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<RuleEntityResponse>> list(
            @RequestParam(required = false) String ruleSystemCode,
            @RequestParam(required = false) String ruleEntityTypeCode,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Boolean active
    ) {
        List<RuleEntityResponse> response = listRuleEntitiesUseCase
                .list(new ListRuleEntitiesQuery(ruleSystemCode, ruleEntityTypeCode, code, active))
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RuleEntityResponse> getById(@PathVariable Long id) {
        return getRuleEntityByIdUseCase.getById(id)
                .map(ruleEntity -> ResponseEntity.ok(toResponse(ruleEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<RuleEntityResponse> search(
            @RequestParam String ruleSystemCode,
            @RequestParam(name = "entityTypeCode") String entityTypeCode,
            @RequestParam String code
    ) {
        return searchRuleEntityByBusinessKeyUseCase.search(ruleSystemCode, entityTypeCode, code)
                .map(ruleEntity -> ResponseEntity.ok(toResponse(ruleEntity)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private RuleEntityResponse toResponse(RuleEntity ruleEntity) {
        return new RuleEntityResponse(
                ruleEntity.getId(),
                ruleEntity.getRuleSystemCode(),
                ruleEntity.getRuleEntityTypeCode(),
                ruleEntity.getCode(),
                ruleEntity.getName(),
                ruleEntity.getDescription(),
                ruleEntity.isActive(),
                ruleEntity.getStartDate(),
                ruleEntity.getEndDate()
        );
    }
}
