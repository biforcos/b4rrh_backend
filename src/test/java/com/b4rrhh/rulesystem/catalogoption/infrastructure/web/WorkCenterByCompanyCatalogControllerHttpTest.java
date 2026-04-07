package com.b4rrhh.rulesystem.catalogoption.infrastructure.web;

import com.b4rrhh.rulesystem.catalogoption.application.query.ListWorkCentersByCompanyQuery;
import com.b4rrhh.rulesystem.catalogoption.application.usecase.ListWorkCentersByCompanyUseCase;
import com.b4rrhh.rulesystem.catalogoption.application.usecase.WorkCentersByCompanyResult;
import com.b4rrhh.rulesystem.catalogoption.domain.model.WorkCenterByCompanyOption;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkCenterByCompanyCatalogControllerHttpTest {

    @Mock
    private ListWorkCentersByCompanyUseCase listWorkCentersByCompanyUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WorkCenterByCompanyCatalogController controller = new WorkCenterByCompanyCatalogController(listWorkCentersByCompanyUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new DirectCatalogOptionExceptionHandler())
                .build();
    }

    @Test
    void returnsWorkCentersByCompany() throws Exception {
        when(listWorkCentersByCompanyUseCase.get(any(ListWorkCentersByCompanyQuery.class)))
                .thenReturn(new WorkCentersByCompanyResult(
                        "ESP",
                        "COMP",
                        LocalDate.of(2026, 4, 15),
                        List.of(new WorkCenterByCompanyOption("MADRID_01", "Madrid 01"))
                ));

        mockMvc.perform(get("/catalog-options/work-centers-by-company")
                        .queryParam("ruleSystemCode", "ESP")
                        .queryParam("companyCode", "COMP")
                        .queryParam("referenceDate", "2026-04-15")
                        .queryParam("q", "madrid"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleSystemCode").value("ESP"))
                .andExpect(jsonPath("$.companyCode").value("COMP"))
                .andExpect(jsonPath("$.items[0].code").value("MADRID_01"))
                .andExpect(jsonPath("$.items[0].name").value("Madrid 01"));

        ArgumentCaptor<ListWorkCentersByCompanyQuery> captor = ArgumentCaptor.forClass(ListWorkCentersByCompanyQuery.class);
        verify(listWorkCentersByCompanyUseCase).get(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("COMP", captor.getValue().companyCode());
        assertEquals(LocalDate.of(2026, 4, 15), captor.getValue().referenceDate());
        assertEquals("madrid", captor.getValue().q());
    }
}
