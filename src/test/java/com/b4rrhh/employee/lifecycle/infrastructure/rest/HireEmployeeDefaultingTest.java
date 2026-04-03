package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;

import com.b4rrhh.employee.lifecycle.application.usecase.HireEmployeeUseCase;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class HireEmployeeDefaultingTest {

    @Mock
    private HireEmployeeUseCase hireEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        HireEmployeeController controller = new HireEmployeeController(hireEmployeeUseCase, new HireEmployeeWebMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    private HireEmployeeResult createMockResult(String rs, String type, String num) {
        LocalDate hireDate = LocalDate.now();
        return new HireEmployeeResult(
                new HireEmployeeResult.EmployeeSummary(rs, type, num, "Ana", "Lopez", null, null, "Ana Lopez", "ACTIVE", hireDate),
                new HireEmployeeResult.PresenceSummary(1, hireDate, "COMP", "HIRE"),
                new HireEmployeeResult.WorkCenterSummary(hireDate, "WC1", "WC1"),
                null,
                new HireEmployeeResult.ContractSummary(hireDate, "CON", "SUB"),
                new HireEmployeeResult.LaborClassificationSummary(hireDate, "AGR", "CAT")
        );
    }

    @Test
    void hireWithExplicitEmpUsesProvidedValue() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenReturn(createMockResult("ESP", "EMP", "E001"));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "ESP",
                                  "employeeTypeCode": "EMP",
                                  "employeeNumber": "E001",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<HireEmployeeCommand> captor = ArgumentCaptor.forClass(HireEmployeeCommand.class);
        verify(hireEmployeeUseCase).hire(captor.capture());
        assertEquals("EMP", captor.getValue().employeeTypeCode());
    }

    @Test
    void hireWithoutEmployeeTypeCodeDefaultsToEmp() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenReturn(createMockResult("ESP", "EMP", "E002"));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "ESP",
                                  "employeeNumber": "E002",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<HireEmployeeCommand> captor = ArgumentCaptor.forClass(HireEmployeeCommand.class);
        verify(hireEmployeeUseCase).hire(captor.capture());
        assertEquals("EMP", captor.getValue().employeeTypeCode());
    }

    @Test
    void hireWithBlankEmployeeTypeCodeDefaultsToEmp() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenReturn(createMockResult("ESP", "EMP", "E003"));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "ESP",
                                  "employeeTypeCode": "   ",
                                  "employeeNumber": "E003",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<HireEmployeeCommand> captor = ArgumentCaptor.forClass(HireEmployeeCommand.class);
        verify(hireEmployeeUseCase).hire(captor.capture());
        assertEquals("EMP", captor.getValue().employeeTypeCode());
    }

    @Test
    void hireWithExplicitNonBlankValuePreservesIt() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenReturn(createMockResult("ESP", "EXT", "E004"));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "ESP",
                                  "employeeTypeCode": "EXT",
                                  "employeeNumber": "E004",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<HireEmployeeCommand> captor = ArgumentCaptor.forClass(HireEmployeeCommand.class);
        verify(hireEmployeeUseCase).hire(captor.capture());
        assertEquals("EXT", captor.getValue().employeeTypeCode());
    }

    @Test
    void hireWithLowercaseValueNormalizesToUppercase() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenReturn(createMockResult("ESP", "EMP", "E005"));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "esp",
                                  "employeeTypeCode": "emp",
                                  "employeeNumber": "E005",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23"
                                }
                                """))
                .andExpect(status().isCreated());

        ArgumentCaptor<HireEmployeeCommand> captor = ArgumentCaptor.forClass(HireEmployeeCommand.class);
        verify(hireEmployeeUseCase).hire(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("EMP", captor.getValue().employeeTypeCode());
    }
}
