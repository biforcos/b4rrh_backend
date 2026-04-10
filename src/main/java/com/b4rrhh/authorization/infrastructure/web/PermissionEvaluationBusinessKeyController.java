package com.b4rrhh.authorization.infrastructure.web;

import com.b4rrhh.authorization.application.usecase.EvaluatePermissionCommand;
import com.b4rrhh.authorization.application.usecase.EvaluatePermissionUseCase;
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

    public PermissionEvaluationBusinessKeyController(EvaluatePermissionUseCase evaluatePermissionUseCase) {
        this.evaluatePermissionUseCase = evaluatePermissionUseCase;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<EvaluatePermissionResponse> evaluate(
            @RequestBody EvaluatePermissionRequest request,
            Authentication authentication
    ) {
        List<String> roleCodes = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .toList();

        PermissionDecision decision = evaluatePermissionUseCase.evaluate(
                new EvaluatePermissionCommand(
                        authentication.getName(),
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
