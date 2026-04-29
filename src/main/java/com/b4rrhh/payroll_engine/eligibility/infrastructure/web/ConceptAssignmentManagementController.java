package com.b4rrhh.payroll_engine.eligibility.infrastructure.web;

import com.b4rrhh.payroll_engine.eligibility.application.usecase.CreateConceptAssignmentUseCase;
import com.b4rrhh.payroll_engine.eligibility.application.usecase.DeleteConceptAssignmentUseCase;
import com.b4rrhh.payroll_engine.eligibility.application.usecase.ListConceptAssignmentsUseCase;
import com.b4rrhh.payroll_engine.eligibility.application.usecase.UpdateConceptAssignmentUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing the eligibility-side {@code ConceptAssignment} aggregate.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>GET {@code /payroll-engine/{ruleSystemCode}/assignments} — list, optionally
 *       filtered by {@code conceptCode}.</li>
 *   <li>POST {@code /payroll-engine/{ruleSystemCode}/assignments} — create.</li>
 *   <li>PUT {@code /payroll-engine/{ruleSystemCode}/assignments/{assignmentCode}} —
 *       update mutable fields. Returns 200 on success and 404 when no assignment matches.</li>
 *   <li>DELETE {@code /payroll-engine/{ruleSystemCode}/assignments/{assignmentCode}} —
 *       remove. Returns 204 on success and 404 when no assignment matches.</li>
 * </ul>
 */
@RestController
@RequestMapping("/payroll-engine/{ruleSystemCode}/assignments")
public class ConceptAssignmentManagementController {

    private final ListConceptAssignmentsUseCase listConceptAssignmentsUseCase;
    private final CreateConceptAssignmentUseCase createConceptAssignmentUseCase;
    private final UpdateConceptAssignmentUseCase updateConceptAssignmentUseCase;
    private final DeleteConceptAssignmentUseCase deleteConceptAssignmentUseCase;
    private final ConceptAssignmentManagementAssembler assembler;

    public ConceptAssignmentManagementController(
            ListConceptAssignmentsUseCase listConceptAssignmentsUseCase,
            CreateConceptAssignmentUseCase createConceptAssignmentUseCase,
            UpdateConceptAssignmentUseCase updateConceptAssignmentUseCase,
            DeleteConceptAssignmentUseCase deleteConceptAssignmentUseCase,
            ConceptAssignmentManagementAssembler assembler
    ) {
        this.listConceptAssignmentsUseCase = listConceptAssignmentsUseCase;
        this.createConceptAssignmentUseCase = createConceptAssignmentUseCase;
        this.updateConceptAssignmentUseCase = updateConceptAssignmentUseCase;
        this.deleteConceptAssignmentUseCase = deleteConceptAssignmentUseCase;
        this.assembler = assembler;
    }

    @GetMapping
    public List<ConceptAssignmentResponse> list(
            @PathVariable String ruleSystemCode,
            @RequestParam(required = false) String conceptCode
    ) {
        return listConceptAssignmentsUseCase.list(ruleSystemCode, conceptCode)
                .stream()
                .map(assembler::toResponse)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConceptAssignmentResponse create(
            @PathVariable String ruleSystemCode,
            @Valid @RequestBody CreateConceptAssignmentRequest request
    ) {
        return assembler.toResponse(
                createConceptAssignmentUseCase.create(assembler.toCommand(ruleSystemCode, request))
        );
    }

    @PutMapping("/{assignmentCode}")
    public ConceptAssignmentResponse update(
            @PathVariable String ruleSystemCode,
            @PathVariable String assignmentCode,
            @Valid @RequestBody UpdateConceptAssignmentRequest request
    ) {
        return assembler.toResponse(
                updateConceptAssignmentUseCase.update(
                        assembler.toUpdateCommand(ruleSystemCode, assignmentCode, request)));
    }

    @DeleteMapping("/{assignmentCode}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable String ruleSystemCode,
            @PathVariable String assignmentCode
    ) {
        deleteConceptAssignmentUseCase.delete(ruleSystemCode, assignmentCode);
    }
}
