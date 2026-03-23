package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.usecase.HireEmployeeUseCase;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeConflictException;
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
class HireEmployeeControllerHttpTest {

    @Mock
    private HireEmployeeUseCase hireEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
      HireEmployeeController controller = new HireEmployeeController(hireEmployeeUseCase, new HireEmployeeWebMapper());
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new HireEmployeeExceptionHandler())
                .build();
    }

    @Test
    void hireReturnsCreatedWithBusinessKeysAndInitialSections() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenReturn(new HireEmployeeResult(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "Ana",
                        "Lopez",
                        null,
                        "Ani",
                        "ACTIVE",
                        LocalDate.of(2026, 3, 23),
                        1,
                        "COMP",
                        "HIRE",
                        "AGR",
                        "CAT",
                        "CON",
                        "SUB",
                        1,
                        "WC1",
                        true
                ));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "esp",
                                  "employeeTypeCode": "internal",
                                  "employeeNumber": "EMP001",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23",
                                  "presence": {
                                    "companyCode": "comp",
                                    "entryReasonCode": "hire"
                                  },
                                  "laborClassification": {
                                    "agreementCode": "agr",
                                    "agreementCategoryCode": "cat"
                                  },
                                  "contract": {
                                    "contractTypeCode": "con",
                                    "contractSubtypeCode": "sub"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "wc1"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ruleSystemCode").value("ESP"))
                .andExpect(jsonPath("$.employeeTypeCode").value("INTERNAL"))
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"))
                .andExpect(jsonPath("$.initialPresence.presenceNumber").value(1))
                .andExpect(jsonPath("$.hireDate[0]").value(2026))
                .andExpect(jsonPath("$.hireDate[1]").value(3))
                .andExpect(jsonPath("$.hireDate[2]").value(23))
                .andExpect(jsonPath("$.initialPresence.startDate[0]").value(2026))
                .andExpect(jsonPath("$.initialPresence.startDate[1]").value(3))
                .andExpect(jsonPath("$.initialPresence.startDate[2]").value(23))
                .andExpect(jsonPath("$.initialLaborClassification.startDate[0]").value(2026))
                .andExpect(jsonPath("$.initialLaborClassification.startDate[1]").value(3))
                .andExpect(jsonPath("$.initialLaborClassification.startDate[2]").value(23))
                .andExpect(jsonPath("$.initialContract.startDate[0]").value(2026))
                .andExpect(jsonPath("$.initialContract.startDate[1]").value(3))
                .andExpect(jsonPath("$.initialContract.startDate[2]").value(23))
                .andExpect(jsonPath("$.initialContract.contractTypeCode").value("CON"))
                .andExpect(jsonPath("$.initialWorkCenter.workCenterAssignmentNumber").value(1))
                .andExpect(jsonPath("$.initialWorkCenter.startDate[0]").value(2026))
                .andExpect(jsonPath("$.initialWorkCenter.startDate[1]").value(3))
                .andExpect(jsonPath("$.initialWorkCenter.startDate[2]").value(23))
                .andExpect(jsonPath("$.id").doesNotExist());

        ArgumentCaptor<HireEmployeeCommand> captor = ArgumentCaptor.forClass(HireEmployeeCommand.class);
        verify(hireEmployeeUseCase).hire(captor.capture());
        assertEquals("esp", captor.getValue().ruleSystemCode());
        assertEquals(LocalDate.of(2026, 3, 23), captor.getValue().hireDate());
    }

    @Test
        void hireReturnsOkWhenIdempotentRetry() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
        .thenReturn(new HireEmployeeResult(
          "ESP",
          "INTERNAL",
          "EMP001",
          "Ana",
          "Lopez",
          null,
          "Ani",
          "ACTIVE",
          LocalDate.of(2026, 3, 23),
          1,
          "COMP",
          "HIRE",
          "AGR",
          "CAT",
          "CON",
          "SUB",
          1,
          "WC1",
          false
        ));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "ESP",
                                  "employeeTypeCode": "INTERNAL",
                                  "employeeNumber": "EMP001",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23",
                                  "presence": {
                                    "companyCode": "COMP",
                                    "entryReasonCode": "HIRE"
                                  },
                                  "laborClassification": {
                                    "agreementCode": "AGR",
                                    "agreementCategoryCode": "CAT"
                                  },
                                  "contract": {
                                    "contractTypeCode": "CON",
                                    "contractSubtypeCode": "SUB"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "WC1"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"));
    }

    @Test
    void hireReturnsConflictWhenStateIsNotEquivalent() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenThrow(new HireEmployeeConflictException("existing hire state is not equivalent"));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "ESP",
                                  "employeeTypeCode": "INTERNAL",
                                  "employeeNumber": "EMP001",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23",
                                  "presence": {
                                    "companyCode": "COMP",
                                    "entryReasonCode": "HIRE"
                                  },
                                  "laborClassification": {
                                    "agreementCode": "AGR",
                                    "agreementCategoryCode": "CAT"
                                  },
                                  "contract": {
                                    "contractTypeCode": "CON",
                                    "contractSubtypeCode": "SUB"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "WC1"
                                  }
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("HIRE_CONFLICT"));
    }

    @Test
    void hireReturnsUnprocessableEntityWhenCatalogValueIsInvalid() throws Exception {
        when(hireEmployeeUseCase.hire(any(HireEmployeeCommand.class)))
                .thenThrow(new HireEmployeeCatalogValueInvalidException("companyCode is invalid", null));

        mockMvc.perform(post("/employees/hire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "ruleSystemCode": "ESP",
                                  "employeeTypeCode": "INTERNAL",
                                  "employeeNumber": "EMP001",
                                  "firstName": "Ana",
                                  "lastName1": "Lopez",
                                  "hireDate": "2026-03-23",
                                  "presence": {
                                    "companyCode": "BAD",
                                    "entryReasonCode": "HIRE"
                                  },
                                  "laborClassification": {
                                    "agreementCode": "AGR",
                                    "agreementCategoryCode": "CAT"
                                  },
                                  "contract": {
                                    "contractTypeCode": "CON",
                                    "contractSubtypeCode": "SUB"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "WC1"
                                  }
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_CATALOG_VALUE"));
    }
}
