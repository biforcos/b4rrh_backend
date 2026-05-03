package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web;

import com.b4rrhh.rulesystem.agreementcategoryprofile.application.usecase.*;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.AgreementCategoryProfile;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model.TipoNomina;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.assembler.AgreementCategoryProfileResponseAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AgreementCategoryProfileControllerHttpTest {

    @Mock private GetAgreementCategoryProfileUseCase getUseCase;
    @Mock private UpsertAgreementCategoryProfileUseCase upsertUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AgreementCategoryProfileController controller = new AgreementCategoryProfileController(
                getUseCase, upsertUseCase, new AgreementCategoryProfileResponseAssembler());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new AgreementCategoryProfileExceptionHandler())
                .build();
    }

    @Test
    void getReturnsExistingProfile() throws Exception {
        when(getUseCase.get(any())).thenReturn(new AgreementCategoryProfile("05", TipoNomina.MENSUAL));

        mockMvc.perform(get("/agreement-categories/ESP/CAT_ADMIN/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryCode").value("CAT_ADMIN"))
                .andExpect(jsonPath("$.grupoCotizacionCode").value("05"))
                .andExpect(jsonPath("$.tipoNomina").value("MENSUAL"));
    }

    @Test
    void putMapsPathAndBodyToCommand() throws Exception {
        when(upsertUseCase.upsert(any())).thenReturn(new AgreementCategoryProfile("03", TipoNomina.MENSUAL));

        mockMvc.perform(put("/agreement-categories/ESP/CAT_ADMIN/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"grupoCotizacionCode": "03", "tipoNomina": "MENSUAL"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupoCotizacionCode").value("03"));

        ArgumentCaptor<UpsertAgreementCategoryProfileCommand> captor =
                ArgumentCaptor.forClass(UpsertAgreementCategoryProfileCommand.class);
        verify(upsertUseCase).upsert(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("CAT_ADMIN", captor.getValue().categoryCode());
        assertEquals("03", captor.getValue().grupoCotizacionCode());
    }

    @Test
    void getMapsCategoryNotFoundToHttp404() throws Exception {
        when(getUseCase.get(any())).thenThrow(new AgreementCategoryProfileCategoryNotFoundException("ESP", "GHOST"));

        mockMvc.perform(get("/agreement-categories/ESP/GHOST/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Agreement category not found")));
    }

    @Test
    void getMapsProfileNotFoundToHttp404() throws Exception {
        when(getUseCase.get(any())).thenThrow(new AgreementCategoryProfileNotFoundException("ESP", "CAT_ADMIN"));

        mockMvc.perform(get("/agreement-categories/ESP/CAT_ADMIN/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("profile not found")));
    }

    @Test
    void putMapsGrupoCotizacionInvalidToHttp422() throws Exception {
        when(upsertUseCase.upsert(any())).thenThrow(new GrupoCotizacionInvalidException("ESP", "99"));

        mockMvc.perform(put("/agreement-categories/ESP/CAT_ADMIN/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"grupoCotizacionCode": "99", "tipoNomina": "MENSUAL"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message", containsString("Grupo de cotización not found")));
    }
}
