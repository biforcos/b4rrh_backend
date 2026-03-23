package com.b4rrhh.employee.contract.infrastructure.rest;

import com.b4rrhh.employee.contract.application.command.ListContractSubtypeCatalogCommand;
import com.b4rrhh.employee.contract.application.model.ContractSubtypeCatalogItem;
import com.b4rrhh.employee.contract.application.usecase.ListContractSubtypeCatalogUseCase;
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
class ContractCatalogControllerHttpTest {

    @Mock
    private ListContractSubtypeCatalogUseCase listContractSubtypeCatalogUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContractCatalogController controller = new ContractCatalogController(listContractSubtypeCatalogUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    void listsContractSubtypesByContractTypeAndReferenceDate() throws Exception {
        when(listContractSubtypeCatalogUseCase.list(any(ListContractSubtypeCatalogCommand.class)))
                .thenReturn(List.of(
                        new ContractSubtypeCatalogItem("FT1", "Full Time", LocalDate.of(2020, 1, 1), null),
                        new ContractSubtypeCatalogItem("PT1", "Part Time", LocalDate.of(2020, 1, 1), null)
                ));

        mockMvc.perform(get("/contract-catalog/contract-subtypes")
                        .queryParam("ruleSystemCode", "esp")
                        .queryParam("contractTypeCode", "ind")
                        .queryParam("referenceDate", "2026-03-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("FT1"))
                .andExpect(jsonPath("$[0].name").value("Full Time"))
                .andExpect(jsonPath("$[0].id").doesNotExist())
                .andExpect(jsonPath("$[1].code").value("PT1"));

        ArgumentCaptor<ListContractSubtypeCatalogCommand> captor =
                ArgumentCaptor.forClass(ListContractSubtypeCatalogCommand.class);
        verify(listContractSubtypeCatalogUseCase).list(captor.capture());
        assertEquals("esp", captor.getValue().ruleSystemCode());
        assertEquals("ind", captor.getValue().contractTypeCode());
        assertEquals(LocalDate.of(2026, 3, 1), captor.getValue().referenceDate());
    }

    @Test
    void listsContractSubtypesWithoutReferenceDate() throws Exception {
        when(listContractSubtypeCatalogUseCase.list(any(ListContractSubtypeCatalogCommand.class)))
                .thenReturn(List.of(
                        new ContractSubtypeCatalogItem("FT1", "Full Time", LocalDate.of(2020, 1, 1), null)
                ));

        mockMvc.perform(get("/contract-catalog/contract-subtypes")
                        .queryParam("ruleSystemCode", "esp")
                        .queryParam("contractTypeCode", "ind"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("FT1"));

        ArgumentCaptor<ListContractSubtypeCatalogCommand> captor =
                ArgumentCaptor.forClass(ListContractSubtypeCatalogCommand.class);
        verify(listContractSubtypeCatalogUseCase).list(captor.capture());
        assertEquals("esp", captor.getValue().ruleSystemCode());
        assertEquals("ind", captor.getValue().contractTypeCode());
        assertNull(captor.getValue().referenceDate());
    }

    @Test
    void returnsEmptyArrayWhenNoSubtypesFound() throws Exception {
        when(listContractSubtypeCatalogUseCase.list(any(ListContractSubtypeCatalogCommand.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/contract-catalog/contract-subtypes")
                        .queryParam("ruleSystemCode", "esp")
                        .queryParam("contractTypeCode", "unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}