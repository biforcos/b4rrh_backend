package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.TerminateEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.TerminateEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.usecase.TerminateEmployeeUseCase;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.TerminateEmployeeConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TerminateEmployeeControllerHttpTest {

    @Mock
    private TerminateEmployeeUseCase terminateEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        TerminateEmployeeController controller = new TerminateEmployeeController(
                terminateEmployeeUseCase,
                new TerminateEmployeeWebMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new TerminateEmployeeExceptionHandler())
                .build();
    }

    @Test
    void terminateReturnsAggregatedClosedBlocks() throws Exception {
        when(terminateEmployeeUseCase.terminate(any(TerminateEmployeeCommand.class)))
                .thenReturn(new TerminateEmployeeResult(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        LocalDate.of(2026, 3, 31),
                        "VOL",
                        "TERMINATED",
                        1,
                        "COMP",
                        "HIRE",
                        "VOL",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 3, 31),
                        "IND",
                        "FT1",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 3, 31),
                        "AGR",
                        "CAT",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 3, 31),
                        1,
                        "WC1",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 3, 31)
                ));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terminationDate": "2026-03-31",
                                  "exitReasonCode": "vol"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleSystemCode").value("ESP"))
                .andExpect(jsonPath("$.employeeTypeCode").value("INTERNAL"))
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"))
                .andExpect(jsonPath("$.terminationDate[0]").value(2026))
                .andExpect(jsonPath("$.terminationDate[1]").value(3))
                .andExpect(jsonPath("$.terminationDate[2]").value(31))
                .andExpect(jsonPath("$.exitReasonCode").value("VOL"))
                .andExpect(jsonPath("$.status").value("TERMINATED"))
                .andExpect(jsonPath("$.closedPresence.presenceNumber").value(1))
                .andExpect(jsonPath("$.closedContract.contractTypeCode").value("IND"))
                .andExpect(jsonPath("$.closedLaborClassification.agreementCode").value("AGR"))
                .andExpect(jsonPath("$.closedWorkCenter.workCenterAssignmentNumber").value(1))
                .andExpect(jsonPath("$.id").doesNotExist());

        ArgumentCaptor<TerminateEmployeeCommand> captor = ArgumentCaptor.forClass(TerminateEmployeeCommand.class);
        verify(terminateEmployeeUseCase).terminate(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals(LocalDate.of(2026, 3, 31), captor.getValue().terminationDate());
        assertEquals("vol", captor.getValue().exitReasonCode());
    }

    @Test
    void terminateReturnsConflictOnFunctionalInconsistency() throws Exception {
        when(terminateEmployeeUseCase.terminate(any(TerminateEmployeeCommand.class)))
                .thenThrow(new TerminateEmployeeConflictException("Expected exactly one active presence but found 2"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terminationDate": "2026-03-31",
                                  "exitReasonCode": "VOL"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("TERMINATE_CONFLICT"));
    }

    @Test
    void terminateReturnsUnprocessableEntityOnInvalidCatalogValue() throws Exception {
        when(terminateEmployeeUseCase.terminate(any(TerminateEmployeeCommand.class)))
                .thenThrow(new TerminateEmployeeCatalogValueInvalidException("exitReasonCode is invalid", null));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/terminate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terminationDate": "2026-03-31",
                                  "exitReasonCode": "BAD"
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_CATALOG_VALUE"));
    }
}
