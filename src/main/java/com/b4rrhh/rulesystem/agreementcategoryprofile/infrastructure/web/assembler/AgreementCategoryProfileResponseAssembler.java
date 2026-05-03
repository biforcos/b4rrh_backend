package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.assembler;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.AgreementCategoryProfileResponse;
import org.springframework.stereotype.Component;

@Component
public class AgreementCategoryProfileResponseAssembler {

    public AgreementCategoryProfileResponse toResponse(String categoryCode, AgreementCategoryProfile profile) {
        return new AgreementCategoryProfileResponse(
                categoryCode.trim().toUpperCase(),
                profile.getGrupoCotizacionCode(),
                profile.getTipoNomina().name()
        );
    }
}
