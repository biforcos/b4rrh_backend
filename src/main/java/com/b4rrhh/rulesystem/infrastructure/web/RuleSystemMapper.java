package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.UpdateRuleSystemCommand;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.infrastructure.web.dto.RuleSystemResponse;
import com.b4rrhh.rulesystem.infrastructure.web.dto.UpdateRuleSystemRequest;

public class RuleSystemMapper {

    public RuleSystemResponse toResponse(RuleSystem ruleSystem) {
        return new RuleSystemResponse(
                ruleSystem.getCode(),
                ruleSystem.getName(),
                ruleSystem.getCountryCode(),
                ruleSystem.isActive()
        );
    }

    public UpdateRuleSystemCommand toUpdateCommand(String ruleSystemCode, UpdateRuleSystemRequest request) {
        return new UpdateRuleSystemCommand(
                ruleSystemCode,
                request.getName(),
                request.getCountryCode(),
                request.getActive()
        );
    }
}
