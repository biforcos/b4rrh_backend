package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web;

import com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase.*;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.assembler.AgreementCategoryProfileResponseAssembler;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.AgreementCategoryProfileResponse;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.UpsertAgreementCategoryProfileRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agreement-categories/{ruleSystemCode}/{categoryCode}/profile")
public class AgreementCategoryProfileController {

    private final GetAgreementCategoryProfileUseCase getUseCase;
    private final UpsertAgreementCategoryProfileUseCase upsertUseCase;
    private final AgreementCategoryProfileResponseAssembler assembler;

    public AgreementCategoryProfileController(
            GetAgreementCategoryProfileUseCase getUseCase,
            UpsertAgreementCategoryProfileUseCase upsertUseCase,
            AgreementCategoryProfileResponseAssembler assembler
    ) {
        this.getUseCase = getUseCase;
        this.upsertUseCase = upsertUseCase;
        this.assembler = assembler;
    }

    @GetMapping
    public ResponseEntity<AgreementCategoryProfileResponse> get(
            @PathVariable String ruleSystemCode,
            @PathVariable String categoryCode
    ) {
        AgreementCategoryProfile profile = getUseCase.get(
                new GetAgreementCategoryProfileQuery(ruleSystemCode, categoryCode));
        return ResponseEntity.ok(assembler.toResponse(categoryCode, profile));
    }

    @PutMapping
    public ResponseEntity<AgreementCategoryProfileResponse> upsert(
            @PathVariable String ruleSystemCode,
            @PathVariable String categoryCode,
            @RequestBody UpsertAgreementCategoryProfileRequest request
    ) {
        AgreementCategoryProfile profile = upsertUseCase.upsert(
                new UpsertAgreementCategoryProfileCommand(
                        ruleSystemCode,
                        categoryCode,
                        request.grupoCotizacionCode(),
                        request.tipoNomina()
                ));
        return ResponseEntity.ok(assembler.toResponse(categoryCode, profile));
    }
}
