package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import com.b4rrhh.payroll_engine.concept.application.usecase.ListConceptFeedsUseCase;
import com.b4rrhh.payroll_engine.concept.application.usecase.ListConceptOperandsUseCase;
import com.b4rrhh.payroll_engine.concept.application.usecase.ReplaceConceptFeedsUseCase;
import com.b4rrhh.payroll_engine.concept.application.usecase.ReplaceConceptOperandsUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for the wiring side of the payroll designer: operand definitions and
 * feed relations attached to a payroll concept identified by its business key.
 *
 * <p>Both PUT endpoints replace the full set of children for the target concept; an empty
 * input list clears every existing row. The PUT operations return the persisted state to
 * keep the client cache aligned without requiring a follow-up GET.
 */
@RestController
@RequestMapping("/payroll-engine/{ruleSystemCode}/concepts/{conceptCode}")
public class ConceptWiringController {

    private final ListConceptOperandsUseCase listConceptOperandsUseCase;
    private final ReplaceConceptOperandsUseCase replaceConceptOperandsUseCase;
    private final ListConceptFeedsUseCase listConceptFeedsUseCase;
    private final ReplaceConceptFeedsUseCase replaceConceptFeedsUseCase;
    private final ConceptWiringAssembler assembler;

    public ConceptWiringController(
            ListConceptOperandsUseCase listConceptOperandsUseCase,
            ReplaceConceptOperandsUseCase replaceConceptOperandsUseCase,
            ListConceptFeedsUseCase listConceptFeedsUseCase,
            ReplaceConceptFeedsUseCase replaceConceptFeedsUseCase,
            ConceptWiringAssembler assembler
    ) {
        this.listConceptOperandsUseCase = listConceptOperandsUseCase;
        this.replaceConceptOperandsUseCase = replaceConceptOperandsUseCase;
        this.listConceptFeedsUseCase = listConceptFeedsUseCase;
        this.replaceConceptFeedsUseCase = replaceConceptFeedsUseCase;
        this.assembler = assembler;
    }

    @GetMapping("/operands")
    public List<ConceptOperandResponse> listOperands(
            @PathVariable String ruleSystemCode,
            @PathVariable String conceptCode
    ) {
        return listConceptOperandsUseCase.list(ruleSystemCode, conceptCode)
                .stream()
                .map(assembler::toOperandResponse)
                .toList();
    }

    @PutMapping("/operands")
    public List<ConceptOperandResponse> replaceOperands(
            @PathVariable String ruleSystemCode,
            @PathVariable String conceptCode,
            @Valid @RequestBody UpdateConceptOperandsRequest request
    ) {
        return replaceConceptOperandsUseCase.replace(
                        assembler.toOperandsCommand(ruleSystemCode, conceptCode, request))
                .stream()
                .map(assembler::toOperandResponse)
                .toList();
    }

    @GetMapping("/feeds")
    public List<ConceptFeedResponse> listFeeds(
            @PathVariable String ruleSystemCode,
            @PathVariable String conceptCode
    ) {
        return listConceptFeedsUseCase.list(ruleSystemCode, conceptCode)
                .stream()
                .map(assembler::toFeedResponse)
                .toList();
    }

    @PutMapping("/feeds")
    public List<ConceptFeedResponse> replaceFeeds(
            @PathVariable String ruleSystemCode,
            @PathVariable String conceptCode,
            @Valid @RequestBody UpdateConceptFeedsRequest request
    ) {
        return replaceConceptFeedsUseCase.replace(
                        assembler.toFeedsCommand(ruleSystemCode, conceptCode, request))
                .stream()
                .map(assembler::toFeedResponse)
                .toList();
    }
}
