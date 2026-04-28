package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import com.b4rrhh.payroll_engine.concept.application.usecase.ReplaceConceptFeedsCommand;
import com.b4rrhh.payroll_engine.concept.application.usecase.ReplaceConceptOperandsCommand;
import com.b4rrhh.payroll_engine.concept.domain.model.OperandRole;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptFeedRelation;
import com.b4rrhh.payroll_engine.concept.domain.model.PayrollConceptOperand;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Translates between the wiring REST DTOs and the application/domain types.
 */
@Component
public class ConceptWiringAssembler {

    public ConceptOperandResponse toOperandResponse(PayrollConceptOperand operand) {
        return new ConceptOperandResponse(
                operand.getOperandRole().name(),
                operand.getSourceObject().getObjectCode()
        );
    }

    public ConceptFeedResponse toFeedResponse(PayrollConceptFeedRelation feed) {
        return new ConceptFeedResponse(
                feed.getSourceObject().getObjectCode(),
                feed.isInvertSign(),
                feed.getEffectiveFrom(),
                feed.getEffectiveTo()
        );
    }

    public ReplaceConceptOperandsCommand toOperandsCommand(
            String ruleSystemCode,
            String conceptCode,
            UpdateConceptOperandsRequest request
    ) {
        List<ReplaceConceptOperandsCommand.Item> items = request.operands().stream()
                .map(item -> new ReplaceConceptOperandsCommand.Item(
                        parseOperandRole(item.operandRole()),
                        item.sourceObjectCode()
                ))
                .toList();
        return new ReplaceConceptOperandsCommand(ruleSystemCode, conceptCode, items);
    }

    public ReplaceConceptFeedsCommand toFeedsCommand(
            String ruleSystemCode,
            String conceptCode,
            UpdateConceptFeedsRequest request
    ) {
        List<ReplaceConceptFeedsCommand.Item> items = request.feeds().stream()
                .map(item -> new ReplaceConceptFeedsCommand.Item(
                        item.sourceObjectCode(),
                        item.invertSign(),
                        item.effectiveFrom(),
                        item.effectiveTo()
                ))
                .toList();
        return new ReplaceConceptFeedsCommand(ruleSystemCode, conceptCode, items);
    }

    private OperandRole parseOperandRole(String value) {
        try {
            return OperandRole.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid operandRole: '" + value + "'");
        }
    }
}
