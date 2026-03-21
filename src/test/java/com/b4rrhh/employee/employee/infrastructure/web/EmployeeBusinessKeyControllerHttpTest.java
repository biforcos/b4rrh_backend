package com.b4rrhh.employee.employee.infrastructure.web;

import com.b4rrhh.employee.employee.application.usecase.GetEmployeeByBusinessKeyUseCase;
import com.b4rrhh.employee.employee.application.usecase.UpdateEmployeeCommand;
import com.b4rrhh.employee.employee.application.usecase.UpdateEmployeeUseCase;
import com.b4rrhh.employee.employee.domain.exception.EmployeeNotFoundException;
import com.b4rrhh.employee.employee.domain.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class EmployeeBusinessKeyControllerHttpTest {

    @Mock
    private GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase;
    @Mock
    private UpdateEmployeeUseCase updateEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        EmployeeBusinessKeyController controller = new EmployeeBusinessKeyController(
                getEmployeeByBusinessKeyUseCase,
                updateEmployeeUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new EmployeeExceptionHandler())
                .build();
    }

    @Test
    void putByBusinessKeyReturnsUpdatedEmployeeWithSameBusinessKey() throws Exception {
        when(updateEmployeeUseCase.update(any(UpdateEmployeeCommand.class)))
                .thenReturn(new Employee(
                        10L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "Alicia",
                        "Garcia",
                        "Perez",
                        "Ali",
                        "ACTIVE",
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));

        mockMvc.perform(put("/employees/ESP/INTERNAL/EMP001")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Alicia",
                                  "lastName1": "Garcia",
                                  "lastName2": "Perez",
                                  "preferredName": "Ali"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ruleSystemCode").value("ESP"))
                .andExpect(jsonPath("$.employeeTypeCode").value("INTERNAL"))
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("Alicia"))
                .andExpect(jsonPath("$.lastName1").value("Garcia"))
                .andExpect(jsonPath("$.lastName2").value("Perez"))
                .andExpect(jsonPath("$.preferredName").value("Ali"));

        ArgumentCaptor<UpdateEmployeeCommand> captor = ArgumentCaptor.forClass(UpdateEmployeeCommand.class);
        verify(updateEmployeeUseCase).update(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals("Alicia", captor.getValue().firstName());
    }

    @Test
    void putByBusinessKeyReturns404WhenEmployeeDoesNotExist() throws Exception {
        when(updateEmployeeUseCase.update(any(UpdateEmployeeCommand.class)))
                .thenThrow(new EmployeeNotFoundException("ESP", "INTERNAL", "EMP404"));

        mockMvc.perform(put("/employees/ESP/INTERNAL/EMP404")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "Alicia",
                                  "lastName1": "Garcia",
                                  "lastName2": "Perez",
                                  "preferredName": "Ali"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void putByBusinessKeyReturns400WhenRequestIsInvalid() throws Exception {
        when(updateEmployeeUseCase.update(any(UpdateEmployeeCommand.class)))
                .thenThrow(new IllegalArgumentException("firstName is required"));

        mockMvc.perform(put("/employees/ESP/INTERNAL/EMP001")
                        .contentType("application/json")
                        .content("""
                                {
                                  "firstName": "  ",
                                  "lastName1": "Garcia"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("firstName is required"));
    }
}