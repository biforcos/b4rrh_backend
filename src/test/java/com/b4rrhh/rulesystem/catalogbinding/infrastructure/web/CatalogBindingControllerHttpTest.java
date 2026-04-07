package com.b4rrhh.rulesystem.catalogbinding.infrastructure.web;

import com.b4rrhh.rulesystem.catalogbinding.application.query.GetCatalogBindingsByResourceQuery;
import com.b4rrhh.rulesystem.catalogbinding.application.usecase.GetCatalogBindingsByResourceUseCase;
import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogFieldBinding;
import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogKind;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CatalogBindingControllerHttpTest {

    @Mock
    private GetCatalogBindingsByResourceUseCase getCatalogBindingsByResourceUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CatalogBindingController controller = new CatalogBindingController(getCatalogBindingsByResourceUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
        void returnsFieldsForExistingResourceCode() throws Exception {
        when(getCatalogBindingsByResourceUseCase.getByResourceCode(any(GetCatalogBindingsByResourceQuery.class)))
                .thenReturn(List.of(new CatalogFieldBinding(
                        "employee.work_center",
                        "workCenterCode",
                        CatalogKind.CUSTOM,
                        null,
                        null,
                        "WORK_CENTER_BY_COMPANY",
                        true
                )));

        mockMvc.perform(get("/catalog-bindings/employee.work_center"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceCode").value("employee.work_center"))
                .andExpect(jsonPath("$.fields[0].fieldCode").value("workCenterCode"))
                .andExpect(jsonPath("$.fields[0].catalogKind").value("CUSTOM"))
                .andExpect(jsonPath("$.fields[0].ruleEntityTypeCode").doesNotExist())
                .andExpect(jsonPath("$.fields[0].dependsOnFieldCode").doesNotExist())
                .andExpect(jsonPath("$.fields[0].customResolverCode").value("WORK_CENTER_BY_COMPANY"))
                .andExpect(jsonPath("$.fields[0].active").value(true))
                .andExpect(jsonPath("$.fields[0].id").doesNotExist());

        ArgumentCaptor<GetCatalogBindingsByResourceQuery> captor =
                ArgumentCaptor.forClass(GetCatalogBindingsByResourceQuery.class);
        verify(getCatalogBindingsByResourceUseCase).getByResourceCode(captor.capture());
        assertEquals("employee.work_center", captor.getValue().resourceCode());
    }

    @Test
        void returnsEmptyFieldsWhenResourceHasNoFields() throws Exception {
        when(getCatalogBindingsByResourceUseCase.getByResourceCode(any(GetCatalogBindingsByResourceQuery.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/catalog-bindings/employee.unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceCode").value("employee.unknown"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.fields").isEmpty());
    }

    @Test
    void returnsEmployeeContractBindingsWithDirectAndDependentFields() throws Exception {
        when(getCatalogBindingsByResourceUseCase.getByResourceCode(any(GetCatalogBindingsByResourceQuery.class)))
                .thenReturn(List.of(
                        new CatalogFieldBinding(
                                "employee.contract",
                                "contractTypeCode",
                                CatalogKind.DIRECT,
                                "CONTRACT",
                                null,
                                null,
                                true
                        ),
                        new CatalogFieldBinding(
                                "employee.contract",
                                "contractSubtypeCode",
                                CatalogKind.DEPENDENT,
                                "CONTRACT_SUBTYPE",
                                "contractTypeCode",
                                null,
                                true
                        )
                ));

        mockMvc.perform(get("/catalog-bindings/employee.contract"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resourceCode").value("employee.contract"))
                .andExpect(jsonPath("$.fields[0].fieldCode").value("contractTypeCode"))
                .andExpect(jsonPath("$.fields[0].catalogKind").value("DIRECT"))
                .andExpect(jsonPath("$.fields[0].ruleEntityTypeCode").value("CONTRACT"))
                .andExpect(jsonPath("$.fields[0].id").doesNotExist())
                .andExpect(jsonPath("$.fields[1].fieldCode").value("contractSubtypeCode"))
                .andExpect(jsonPath("$.fields[1].catalogKind").value("DEPENDENT"))
                .andExpect(jsonPath("$.fields[1].ruleEntityTypeCode").value("CONTRACT_SUBTYPE"))
                .andExpect(jsonPath("$.fields[1].dependsOnFieldCode").value("contractTypeCode"))
                .andExpect(jsonPath("$.fields[1].id").doesNotExist());
    }
}