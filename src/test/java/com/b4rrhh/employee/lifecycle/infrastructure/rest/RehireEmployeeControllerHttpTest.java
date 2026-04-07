package com.b4rrhh.employee.lifecycle.infrastructure.rest;

import com.b4rrhh.employee.lifecycle.application.command.RehireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.RehireEmployeeResult;
import com.b4rrhh.employee.lifecycle.application.usecase.RehireEmployeeUseCase;
import com.b4rrhh.employee.lifecycle.domain.exception.RehireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.RehireEmployeeConflictException;
import com.b4rrhh.employee.lifecycle.domain.exception.RehireEmployeeDependentRelationInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.RehireEmployeeDistributionInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCompanyMismatchException;
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
class RehireEmployeeControllerHttpTest {

    @Mock
    private RehireEmployeeUseCase rehireEmployeeUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RehireEmployeeController controller = new RehireEmployeeController(
                rehireEmployeeUseCase,
                new RehireEmployeeWebMapper()
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RehireEmployeeExceptionHandler())
                .build();
    }

    @Test
    void rehireReturnsAggregatedNewBlocks() throws Exception {
        when(rehireEmployeeUseCase.rehire(any(RehireEmployeeCommand.class)))
                .thenReturn(new RehireEmployeeResult(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        LocalDate.of(2026, 4, 15),
                        "ACTIVE",
                        2,
                        "ES01",
                        "REHIRE",
                        LocalDate.of(2026, 4, 15),
                        "PERMANENT",
                        "ORDINARY",
                        LocalDate.of(2026, 4, 15),
                        "METAL",
                        "OFICIAL_1",
                        LocalDate.of(2026, 4, 15),
                        2,
                        "MADRID_01",
                        LocalDate.of(2026, 4, 15),
                        null,
                        true
                ));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "rehire",
                                  "companyCode": "es01",
                                  "laborClassification": {
                                    "agreementCode": "metal",
                                    "agreementCategoryCode": "oficial_1"
                                  },
                                  "contract": {
                                    "contractTypeCode": "permanent",
                                    "contractSubtypeCode": "ordinary"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "madrid_01"
                                  }
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ruleSystemCode").value("ESP"))
                .andExpect(jsonPath("$.employeeTypeCode").value("INTERNAL"))
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"))
                .andExpect(jsonPath("$.rehireDate[0]").value(2026))
                .andExpect(jsonPath("$.rehireDate[1]").value(4))
                .andExpect(jsonPath("$.rehireDate[2]").value(15))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.newPresence.presenceNumber").value(2))
                .andExpect(jsonPath("$.newContract.contractTypeCode").value("PERMANENT"))
                .andExpect(jsonPath("$.newLaborClassification.agreementCode").value("METAL"))
                .andExpect(jsonPath("$.newWorkCenter.workCenterAssignmentNumber").value(2))
                .andExpect(jsonPath("$.newCostCenter").doesNotExist());

        ArgumentCaptor<RehireEmployeeCommand> captor = ArgumentCaptor.forClass(RehireEmployeeCommand.class);
        verify(rehireEmployeeUseCase).rehire(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals(LocalDate.of(2026, 4, 15), captor.getValue().rehireDate());
        assertEquals("rehire", captor.getValue().entryReasonCode());
    }

    @Test
    void rehireReturnsOkWhenIdempotentReplay() throws Exception {
        when(rehireEmployeeUseCase.rehire(any(RehireEmployeeCommand.class)))
                .thenReturn(new RehireEmployeeResult(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        LocalDate.of(2026, 4, 15),
                        "ACTIVE",
                        2,
                        "ES01",
                        "REHIRE",
                        LocalDate.of(2026, 4, 15),
                        "PERMANENT",
                        "ORDINARY",
                        LocalDate.of(2026, 4, 15),
                        "METAL",
                        "OFICIAL_1",
                        LocalDate.of(2026, 4, 15),
                        2,
                        "MADRID_01",
                        LocalDate.of(2026, 4, 15),
                        null,
                        false
                ));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "OFICIAL_1"
                                  },
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  }
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeNumber").value("EMP001"));
    }

    @Test
    void rehireReturnsConflictOnFunctionalInconsistency() throws Exception {
        when(rehireEmployeeUseCase.rehire(any(RehireEmployeeCommand.class)))
                .thenThrow(new RehireEmployeeConflictException("Active cycle exists and is not equivalent"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "OFICIAL_1"
                                  },
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  }
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REHIRE_CONFLICT"));
    }

    @Test
    void rehireReturnsUnprocessableEntityOnInvalidCatalogValue() throws Exception {
        when(rehireEmployeeUseCase.rehire(any(RehireEmployeeCommand.class)))
                .thenThrow(new RehireEmployeeCatalogValueInvalidException("entryReasonCode is invalid", null));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "BAD",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "OFICIAL_1"
                                  },
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  }
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_CATALOG_VALUE"));
    }

    @Test
    void rehireReturnsUnprocessableEntityOnInvalidDependentRelation() throws Exception {
        when(rehireEmployeeUseCase.rehire(any(RehireEmployeeCommand.class)))
                .thenThrow(new RehireEmployeeDependentRelationInvalidException(
                        "agreementCategory OFICIAL_1 does not belong to agreement METAL", null));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "INVALID_CAT"
                                  },
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  }
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_DEPENDENT_RELATION"));
    }

    @Test
    void rehireReturnsUnprocessableEntityOnInvalidDistribution() throws Exception {
        when(rehireEmployeeUseCase.rehire(any(RehireEmployeeCommand.class)))
                .thenThrow(new RehireEmployeeDistributionInvalidException("total allocation percentage exceeds 100"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "OFICIAL_1"
                                  },
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  },
                                  "costCenterDistribution": {
                                    "items": [
                                      { "costCenterCode": "CC1", "allocationPercentage": 80.0 },
                                      { "costCenterCode": "CC2", "allocationPercentage": 80.0 }
                                    ]
                                  }
                                }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INVALID_DISTRIBUTION"));
    }

    @Test
    void rehireReturnsConflictWhenWorkCenterBelongsToDifferentCompany() throws Exception {
        when(rehireEmployeeUseCase.rehire(any(RehireEmployeeCommand.class)))
                .thenThrow(new WorkCenterCompanyMismatchException("MADRID_01", "ES01"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "OFICIAL_1"
                                  },
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  }
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("WORK_CENTER_COMPANY_MISMATCH"));
    }

    @Test
    void rehireReturnsBadRequestWhenLaborClassificationBlockIsMissing() throws Exception {
        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REHIRE_REQUEST_INVALID"))
                .andExpect(jsonPath("$.message").value("laborClassification is required"));
    }

    @Test
    void rehireReturnsBadRequestWhenContractBlockIsMissing() throws Exception {
        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "OFICIAL_1"
                                  },
                                  "workCenter": {
                                    "workCenterCode": "MADRID_01"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REHIRE_REQUEST_INVALID"))
                .andExpect(jsonPath("$.message").value("contract is required"));
    }

    @Test
    void rehireReturnsBadRequestWhenWorkCenterBlockIsMissing() throws Exception {
        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/rehire")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rehireDate": "2026-04-15",
                                  "entryReasonCode": "REHIRE",
                                  "companyCode": "ES01",
                                  "laborClassification": {
                                    "agreementCode": "METAL",
                                    "agreementCategoryCode": "OFICIAL_1"
                                  },
                                  "contract": {
                                    "contractTypeCode": "PERMANENT",
                                    "contractSubtypeCode": "ORDINARY"
                                  }
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REHIRE_REQUEST_INVALID"))
                .andExpect(jsonPath("$.message").value("workCenter is required"));
    }
}
