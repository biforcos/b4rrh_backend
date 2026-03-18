package com.b4rrhh.employee.cost_center.infrastructure.rest;

import com.b4rrhh.employee.cost_center.application.command.CreateCostCenterCommand;
import com.b4rrhh.employee.cost_center.application.usecase.CloseCostCenterUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.CreateCostCenterUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.GetCostCenterByBusinessKeyUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.ListEmployeeCostCentersUseCase;
import com.b4rrhh.employee.cost_center.application.usecase.UpdateCostCenterUseCase;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterAllocationNotFoundException;
import com.b4rrhh.employee.cost_center.domain.exception.CostCenterOverlapException;
import com.b4rrhh.employee.cost_center.domain.exception.InvalidAllocationPercentageException;
import com.b4rrhh.employee.cost_center.domain.model.CostCenterAllocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CostCenterControllerHttpTest {

    @Mock
    private CreateCostCenterUseCase createCostCenterUseCase;
    @Mock
    private ListEmployeeCostCentersUseCase listEmployeeCostCentersUseCase;
    @Mock
    private GetCostCenterByBusinessKeyUseCase getCostCenterByBusinessKeyUseCase;
    @Mock
    private UpdateCostCenterUseCase updateCostCenterUseCase;
    @Mock
    private CloseCostCenterUseCase closeCostCenterUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CostCenterController controller = new CostCenterController(
                createCostCenterUseCase,
                listEmployeeCostCentersUseCase,
                getCostCenterByBusinessKeyUseCase,
                updateCostCenterUseCase,
                closeCostCenterUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new CostCenterExceptionHandler())
                .build();
    }

    @Test
    void createMapsPathAndBodyToCommandAndHidesTechnicalIds() throws Exception {
        when(createCostCenterUseCase.create(any(CreateCostCenterCommand.class)))
                .thenReturn(costCenter("CC01", new BigDecimal("50"), LocalDate.of(2026, 1, 1), null));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/cost-centers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costCenterCode": "CC01",
                                  "allocationPercentage": 50,
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.costCenterCode").value("CC01"))
                .andExpect(jsonPath("$.allocationPercentage").value(50))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.employeeId").doesNotExist());

        ArgumentCaptor<CreateCostCenterCommand> captor = ArgumentCaptor.forClass(CreateCostCenterCommand.class);
        verify(createCostCenterUseCase).create(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals("CC01", captor.getValue().costCenterCode());
    }

    @Test
    void hasNoTechnicalIdRouteForSingleSegmentChildId() throws Exception {
        mockMvc.perform(get("/employees/ESP/INTERNAL/EMP001/cost-centers/123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void mapsBadRequestToHttp400() throws Exception {
        when(createCostCenterUseCase.create(any(CreateCostCenterCommand.class)))
                .thenThrow(new InvalidAllocationPercentageException("allocationPercentage must be greater than 0"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/cost-centers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costCenterCode": "CC01",
                                  "allocationPercentage": 0,
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("allocationPercentage")));
    }

    @Test
    void mapsConflictToHttp409() throws Exception {
        when(createCostCenterUseCase.create(any(CreateCostCenterCommand.class)))
                .thenThrow(new CostCenterOverlapException(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "CC01",
                        LocalDate.of(2026, 1, 1),
                        null
                ));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/cost-centers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "costCenterCode": "CC01",
                                  "allocationPercentage": 50,
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("overlaps")));
    }

    @Test
    void mapsNotFoundToHttp404() throws Exception {
        when(getCostCenterByBusinessKeyUseCase.getByBusinessKey(any()))
                .thenThrow(new CostCenterAllocationNotFoundException(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "CC01",
                        LocalDate.of(2026, 1, 1)
                ));

        mockMvc.perform(get("/employees/ESP/INTERNAL/EMP001/cost-centers/CC01/2026-01-01"))
                .andExpect(status().isNotFound());
    }

    private CostCenterAllocation costCenter(
            String costCenterCode,
            BigDecimal allocationPercentage,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new CostCenterAllocation(
                10L,
                costCenterCode,
                allocationPercentage,
                startDate,
                endDate
        );
    }
}
