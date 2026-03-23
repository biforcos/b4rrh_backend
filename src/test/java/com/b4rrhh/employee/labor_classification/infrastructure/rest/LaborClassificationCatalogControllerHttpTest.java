package com.b4rrhh.employee.labor_classification.infrastructure.rest;

import com.b4rrhh.employee.labor_classification.application.command.ListAgreementCategoryCatalogCommand;
import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;
import com.b4rrhh.employee.labor_classification.application.usecase.ListAgreementCategoryCatalogUseCase;
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class LaborClassificationCatalogControllerHttpTest {

    @Mock
    private ListAgreementCategoryCatalogUseCase listAgreementCategoryCatalogUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LaborClassificationCatalogController controller =
                new LaborClassificationCatalogController(listAgreementCategoryCatalogUseCase);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new LaborClassificationExceptionHandler())
                .build();
    }

    @Test
    void listsAgreementCategoriesByAgreementAndReferenceDate() throws Exception {
        when(listAgreementCategoryCatalogUseCase.list(any(ListAgreementCategoryCatalogCommand.class)))
                .thenReturn(List.of(
                        new AgreementCategoryCatalogItem("CAT_A", "Category A", LocalDate.of(2020, 1, 1), null),
                        new AgreementCategoryCatalogItem("CAT_B", "Category B", LocalDate.of(2022, 1, 1), null)
                ));

        mockMvc.perform(get("/labor-classification-catalog/agreement-categories")
                        .queryParam("ruleSystemCode", "esp")
                        .queryParam("agreementCode", "agr_01")
                        .queryParam("referenceDate", "2026-03-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CAT_A"))
                .andExpect(jsonPath("$[0].name").value("Category A"))
                .andExpect(jsonPath("$[1].code").value("CAT_B"));

        ArgumentCaptor<ListAgreementCategoryCatalogCommand> captor =
                ArgumentCaptor.forClass(ListAgreementCategoryCatalogCommand.class);
        verify(listAgreementCategoryCatalogUseCase).list(captor.capture());
        assertEquals("esp", captor.getValue().ruleSystemCode());
        assertEquals("agr_01", captor.getValue().agreementCode());
        assertEquals(LocalDate.of(2026, 3, 1), captor.getValue().referenceDate());
    }

    @Test
    void listsAgreementCategoriesWithoutReferenceDate() throws Exception {
        when(listAgreementCategoryCatalogUseCase.list(any(ListAgreementCategoryCatalogCommand.class)))
                .thenReturn(List.of(
                        new AgreementCategoryCatalogItem("CAT_A", "Category A", LocalDate.of(2020, 1, 1), null)
                ));

        mockMvc.perform(get("/labor-classification-catalog/agreement-categories")
                        .queryParam("ruleSystemCode", "esp")
                        .queryParam("agreementCode", "agr_01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("CAT_A"));

        ArgumentCaptor<ListAgreementCategoryCatalogCommand> captor =
                ArgumentCaptor.forClass(ListAgreementCategoryCatalogCommand.class);
        verify(listAgreementCategoryCatalogUseCase).list(captor.capture());
        assertEquals("esp", captor.getValue().ruleSystemCode());
        assertEquals("agr_01", captor.getValue().agreementCode());
        assertNull(captor.getValue().referenceDate());
    }
}
