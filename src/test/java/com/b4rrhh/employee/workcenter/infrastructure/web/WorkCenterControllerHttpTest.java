package com.b4rrhh.employee.workcenter.infrastructure.web;

import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CloseWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.employee.workcenter.application.usecase.CreateWorkCenterUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.GetWorkCenterByBusinessKeyUseCase;
import com.b4rrhh.employee.workcenter.application.usecase.ListEmployeeWorkCentersUseCase;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOverlapException;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class WorkCenterControllerHttpTest {

    @Mock
    private CreateWorkCenterUseCase createWorkCenterUseCase;
    @Mock
    private CloseWorkCenterUseCase closeWorkCenterUseCase;
    @Mock
    private GetWorkCenterByBusinessKeyUseCase getWorkCenterByBusinessKeyUseCase;
    @Mock
    private ListEmployeeWorkCentersUseCase listEmployeeWorkCentersUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        WorkCenterController controller = new WorkCenterController(
                createWorkCenterUseCase,
                closeWorkCenterUseCase,
                getWorkCenterByBusinessKeyUseCase,
                listEmployeeWorkCentersUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new WorkCenterExceptionHandler())
                .build();
    }

    @Test
    void createMapsPathAndBodyToCommand() throws Exception {
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class)))
                .thenReturn(workCenter(1, "MADRID_HQ", LocalDate.of(2026, 1, 10), null));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/work-centers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workCenterCode": "MADRID_HQ",
                                  "startDate": "2026-01-10"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workCenterAssignmentNumber").value(1))
                .andExpect(jsonPath("$.workCenterCode").value("MADRID_HQ"));

        ArgumentCaptor<CreateWorkCenterCommand> captor = ArgumentCaptor.forClass(CreateWorkCenterCommand.class);
        verify(createWorkCenterUseCase).create(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals("MADRID_HQ", captor.getValue().workCenterCode());
    }

    @Test
    void closeMapsPathAndBodyToCommand() throws Exception {
        when(closeWorkCenterUseCase.close(any(CloseWorkCenterCommand.class)))
                .thenReturn(workCenter(1, "MADRID_HQ", LocalDate.of(2026, 1, 10), LocalDate.of(2026, 1, 20)));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/work-centers/1/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endDate": "2026-01-20"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workCenterAssignmentNumber").value(1))
                .andExpect(jsonPath("$.endDate[0]").value(2026))
                .andExpect(jsonPath("$.endDate[1]").value(1))
                .andExpect(jsonPath("$.endDate[2]").value(20));

        ArgumentCaptor<CloseWorkCenterCommand> captor = ArgumentCaptor.forClass(CloseWorkCenterCommand.class);
        verify(closeWorkCenterUseCase).close(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(1, captor.getValue().workCenterAssignmentNumber());
    }

    @Test
    void createMapsDomainConflictToHttp409() throws Exception {
        when(createWorkCenterUseCase.create(any(CreateWorkCenterCommand.class)))
                .thenThrow(new WorkCenterOverlapException("ESP", "INTERNAL", "EMP001"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/work-centers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "workCenterCode": "MADRID_HQ",
                                  "startDate": "2026-01-10"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Work center period overlaps")));
    }

    private WorkCenter workCenter(
            int assignmentNumber,
            String workCenterCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new WorkCenter(
                1L,
                10L,
                assignmentNumber,
                workCenterCode,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}