package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.CloseRuleEntityCommand;
import com.b4rrhh.rulesystem.application.usecase.CloseRuleEntityUseCase;
import com.b4rrhh.rulesystem.application.usecase.CorrectRuleEntityCommand;
import com.b4rrhh.rulesystem.application.usecase.CorrectRuleEntityUseCase;
import com.b4rrhh.rulesystem.application.usecase.DeleteRuleEntityCommand;
import com.b4rrhh.rulesystem.application.usecase.DeleteRuleEntityUseCase;
import com.b4rrhh.rulesystem.application.usecase.GetRuleEntityByBusinessKeyQuery;
import com.b4rrhh.rulesystem.application.usecase.GetRuleEntityByBusinessKeyUseCase;
import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.infrastructure.web.dto.CloseRuleEntityRequest;
import com.b4rrhh.rulesystem.infrastructure.web.dto.CorrectRuleEntityRequest;
import com.b4rrhh.rulesystem.infrastructure.web.dto.RuleEntityResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}")
public class RuleEntityBusinessKeyController {

    private final GetRuleEntityByBusinessKeyUseCase getRuleEntityByBusinessKeyUseCase;
    private final CorrectRuleEntityUseCase correctRuleEntityUseCase;
    private final CloseRuleEntityUseCase closeRuleEntityUseCase;
    private final DeleteRuleEntityUseCase deleteRuleEntityUseCase;

    public RuleEntityBusinessKeyController(
        GetRuleEntityByBusinessKeyUseCase getRuleEntityByBusinessKeyUseCase,
        CorrectRuleEntityUseCase correctRuleEntityUseCase,
        CloseRuleEntityUseCase closeRuleEntityUseCase,
        DeleteRuleEntityUseCase deleteRuleEntityUseCase
    ) {
    this.getRuleEntityByBusinessKeyUseCase = getRuleEntityByBusinessKeyUseCase;
    this.correctRuleEntityUseCase = correctRuleEntityUseCase;
    this.closeRuleEntityUseCase = closeRuleEntityUseCase;
        this.deleteRuleEntityUseCase = deleteRuleEntityUseCase;
    }

    @GetMapping
    public ResponseEntity<RuleEntityResponse> getByBusinessKey(
        @PathVariable String ruleSystemCode,
        @PathVariable String ruleEntityTypeCode,
        @PathVariable String code,
        @PathVariable LocalDate startDate
    ) {
    RuleEntity ruleEntity = getRuleEntityByBusinessKeyUseCase.get(
        new GetRuleEntityByBusinessKeyQuery(ruleSystemCode, ruleEntityTypeCode, code, startDate)
    );

    return ResponseEntity.ok(toResponse(ruleEntity));
    }

    @PutMapping
    public ResponseEntity<RuleEntityResponse> correctByBusinessKey(
        @PathVariable String ruleSystemCode,
        @PathVariable String ruleEntityTypeCode,
        @PathVariable String code,
        @PathVariable LocalDate startDate,
        @RequestBody CorrectRuleEntityRequest request
    ) {
    RuleEntity corrected = correctRuleEntityUseCase.correct(
        new CorrectRuleEntityCommand(
            ruleSystemCode,
            ruleEntityTypeCode,
            code,
            startDate,
            request.name(),
            request.description(),
            request.endDate()
        )
    );

    return ResponseEntity.ok(toResponse(corrected));
    }

    @PostMapping("/close")
    public ResponseEntity<RuleEntityResponse> closeByBusinessKey(
        @PathVariable String ruleSystemCode,
        @PathVariable String ruleEntityTypeCode,
        @PathVariable String code,
        @PathVariable LocalDate startDate,
        @RequestBody CloseRuleEntityRequest request
    ) {
    RuleEntity closed = closeRuleEntityUseCase.close(
        new CloseRuleEntityCommand(
            ruleSystemCode,
            ruleEntityTypeCode,
            code,
            startDate,
            request.endDate()
        )
    );

    return ResponseEntity.ok(toResponse(closed));
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String ruleEntityTypeCode,
            @PathVariable String code,
            @PathVariable LocalDate startDate
    ) {
        deleteRuleEntityUseCase.delete(
                new DeleteRuleEntityCommand(ruleSystemCode, ruleEntityTypeCode, code, startDate)
        );

        return ResponseEntity.noContent().build();
    }

    private RuleEntityResponse toResponse(RuleEntity ruleEntity) {
        return new RuleEntityResponse(
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
