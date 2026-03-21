package com.b4rrhh.rulesystem.infrastructure.persistence;

import com.b4rrhh.rulesystem.application.port.RuleEntityUsageCheckPort;
import org.springframework.stereotype.Component;

@Component
public class RuleEntityUsageCheckAdapter implements RuleEntityUsageCheckPort {

    @Override
    public boolean isRuleEntityUsed(String ruleSystemCode, String ruleEntityTypeCode, String code) {
        // TODO Check references in presence, contact, address, and other business resources.
        return false;
    }
}
