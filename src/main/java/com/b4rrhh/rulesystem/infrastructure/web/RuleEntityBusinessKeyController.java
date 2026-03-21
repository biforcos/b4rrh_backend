package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.DeleteRuleEntityCommand;
import com.b4rrhh.rulesystem.application.usecase.DeleteRuleEntityUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/rule-entities/{ruleSystemCode}/{ruleEntityTypeCode}/{code}/{startDate}")
public class RuleEntityBusinessKeyController {

    private final DeleteRuleEntityUseCase deleteRuleEntityUseCase;

    public RuleEntityBusinessKeyController(DeleteRuleEntityUseCase deleteRuleEntityUseCase) {
        this.deleteRuleEntityUseCase = deleteRuleEntityUseCase;
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
}
