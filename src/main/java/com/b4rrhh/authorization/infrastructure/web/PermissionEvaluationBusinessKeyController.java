package com.b4rrhh.authorization.infrastructure.web;

import com.b4rrhh.authorization.application.usecase.EvaluatePermissionCommand;
import com.b4rrhh.authorization.application.usecase.EvaluatePermissionUseCase;
import com.b4rrhh.authorization.application.usecase.ResolveSubjectRolesUseCase;
import com.b4rrhh.authorization.domain.model.PermissionDecision;
import com.b4rrhh.authorization.infrastructure.web.dto.EvaluatePermissionRequest;
import com.b4rrhh.authorization.infrastructure.web.dto.EvaluatePermissionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/authorization")
public class PermissionEvaluationBusinessKeyController {

    private final EvaluatePermissionUseCase evaluatePermissionUseCase;
        private final ResolveSubjectRolesUseCase resolveSubjectRolesUseCase;

        public PermissionEvaluationBusinessKeyController(
                        EvaluatePermissionUseCase evaluatePermissionUseCase,
                        ResolveSubjectRolesUseCase resolveSubjectRolesUseCase
        ) {
        this.evaluatePermissionUseCase = evaluatePermissionUseCase;
                this.resolveSubjectRolesUseCase = resolveSubjectRolesUseCase;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluatePermissionResponse> evaluate(
            @RequestBody EvaluatePermissionRequest request,
            Authentication authentication
    ) {
        String subjectCode = authentication.getName();
        List<String> roleCodes = resolveSubjectRolesUseCase.resolveActiveRoleCodes(subjectCode);

        PermissionDecision decision = evaluatePermissionUseCase.evaluate(
                new EvaluatePermissionCommand(
                        subjectCode,
                        roleCodes,
                        request.resourceCode(),
                        request.actionCode()
                )
        );

        return ResponseEntity.ok(new EvaluatePermissionResponse(
                decision.decision().name(),
                decision.reason()
        ));
    }
}
