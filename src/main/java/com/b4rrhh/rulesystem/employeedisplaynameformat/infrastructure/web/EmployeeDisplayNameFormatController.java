package com.b4rrhh.rulesystem.employeedisplaynameformat.infrastructure.web;

import com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase.GetEmployeeDisplayNameFormatUseCase;
import com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase.UpsertEmployeeDisplayNameFormatCommand;
import com.b4rrhh.rulesystem.employeedisplaynameformat.application.usecase.UpsertEmployeeDisplayNameFormatUseCase;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatCode;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.DisplayNameFormatter;
import com.b4rrhh.rulesystem.employeedisplaynameformat.domain.model.EmployeeDisplayNameFormat;
import com.b4rrhh.rulesystem.employeedisplaynameformat.infrastructure.web.dto.EmployeeDisplayNameFormatResponse;
import com.b4rrhh.rulesystem.employeedisplaynameformat.infrastructure.web.dto.UpsertEmployeeDisplayNameFormatRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rule-systems/{ruleSystemCode}/employee-display-name-format")
public class EmployeeDisplayNameFormatController {

    private static final String EXAMPLE_FIRST = "Juan Antonio";
    private static final String EXAMPLE_LAST1 = "Biforcos";
    private static final String EXAMPLE_LAST2 = "Amor";

    private final GetEmployeeDisplayNameFormatUseCase getUseCase;
    private final UpsertEmployeeDisplayNameFormatUseCase upsertUseCase;

    public EmployeeDisplayNameFormatController(
            GetEmployeeDisplayNameFormatUseCase getUseCase,
            UpsertEmployeeDisplayNameFormatUseCase upsertUseCase) {
        this.getUseCase = getUseCase;
        this.upsertUseCase = upsertUseCase;
    }

    @GetMapping
    public ResponseEntity<EmployeeDisplayNameFormatResponse> get(
            @PathVariable String ruleSystemCode) {
        return getUseCase.getByRuleSystemCode(ruleSystemCode)
                .map(this::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<EmployeeDisplayNameFormatResponse> upsert(
            @PathVariable String ruleSystemCode,
            @RequestBody UpsertEmployeeDisplayNameFormatRequest request) {
        EmployeeDisplayNameFormat format = upsertUseCase.upsert(
                new UpsertEmployeeDisplayNameFormatCommand(ruleSystemCode, request.formatCode()));
        return ResponseEntity.ok(toResponse(format));
    }

    private EmployeeDisplayNameFormatResponse toResponse(EmployeeDisplayNameFormat format) {
        String example = DisplayNameFormatter.format(
                EXAMPLE_FIRST, EXAMPLE_LAST1, EXAMPLE_LAST2, format.formatCode());
        return new EmployeeDisplayNameFormatResponse(
                format.ruleSystemCode(),
                format.formatCode().name(),
                formatLabel(format.formatCode()),
                example
        );
    }

    private String formatLabel(DisplayNameFormatCode code) {
        return switch (code) {
            case FULL_TITLE_CASE -> "Nombre completo (mayúsculas iniciales)";
            case FULL_UPPER -> "Nombre completo en mayúsculas";
            case SURNAME_FIRST_UPPER -> "Apellidos, Nombre (mayúsculas)";
            case SHORT_TITLE -> "Nombre y primer apellido (mayúsculas iniciales)";
            case SHORT_UPPER -> "Nombre y primer apellido (mayúsculas)";
            case SURNAME_ABBREV_UPPER -> "Apellidos, iniciales del nombre";
        };
    }
}
