package com.b4rrhh.employee.contract.infrastructure.rest;

import com.b4rrhh.employee.contract.application.command.CloseContractCommand;
import com.b4rrhh.employee.contract.application.command.CreateContractCommand;
import com.b4rrhh.employee.contract.application.command.GetContractByBusinessKeyCommand;
import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.command.ReplaceContractFromDateCommand;
import com.b4rrhh.employee.contract.application.command.UpdateContractCommand;
import com.b4rrhh.employee.contract.application.usecase.CloseContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.CreateContractUseCase;
import com.b4rrhh.employee.contract.application.usecase.GetContractByBusinessKeyUseCase;
import com.b4rrhh.employee.contract.application.usecase.ListEmployeeContractsUseCase;
import com.b4rrhh.employee.contract.application.usecase.ReplaceContractFromDateUseCase;
import com.b4rrhh.employee.contract.application.usecase.UpdateContractUseCase;
import com.b4rrhh.employee.contract.domain.exception.ContractInvalidException;
import com.b4rrhh.employee.contract.domain.exception.ContractNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractOverlapException;
import com.b4rrhh.employee.contract.domain.exception.ContractSubtypeInvalidException;
import com.b4rrhh.employee.contract.domain.model.Contract;
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
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContractControllerHttpTest {

    @Mock
    private CreateContractUseCase createContractUseCase;
    @Mock
    private ListEmployeeContractsUseCase listEmployeeContractsUseCase;
    @Mock
    private GetContractByBusinessKeyUseCase getContractByBusinessKeyUseCase;
    @Mock
    private UpdateContractUseCase updateContractUseCase;
    @Mock
    private CloseContractUseCase closeContractUseCase;
        @Mock
        private ReplaceContractFromDateUseCase replaceContractFromDateUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ContractController controller = new ContractController(
                createContractUseCase,
                listEmployeeContractsUseCase,
                getContractByBusinessKeyUseCase,
                updateContractUseCase,
                closeContractUseCase,
                replaceContractFromDateUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ContractExceptionHandler())
                .build();
    }

    @Test
    void createMapsPathAndBodyToCommandAndHidesTechnicalIds() throws Exception {
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenReturn(contract("IND", "FT1", LocalDate.of(2026, 1, 1), null));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractCode": "IND",
                                  "contractSubtypeCode": "FT1",
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.contractCode").value("IND"))
                .andExpect(jsonPath("$.contractSubtypeCode").value("FT1"))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.employeeId").doesNotExist());

        ArgumentCaptor<CreateContractCommand> captor =
                ArgumentCaptor.forClass(CreateContractCommand.class);
        verify(createContractUseCase).create(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(LocalDate.of(2026, 1, 1), captor.getValue().startDate());
    }

    @Test
    void listMapsPathToCommandAndReturns200() throws Exception {
        when(listEmployeeContractsUseCase.listByEmployeeBusinessKey(any(ListEmployeeContractsCommand.class)))
                .thenReturn(List.of(
                        contract("IND", "FT1", LocalDate.of(2026, 1, 1), null),
                        contract("TMP", "PT1", LocalDate.of(2026, 2, 1), null)
                ));

        mockMvc.perform(get("/employees/ESP/INTERNAL/EMP001/contracts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].contractCode").value("IND"))
                .andExpect(jsonPath("$[0].contractSubtypeCode").value("FT1"))
                .andExpect(jsonPath("$[1].contractCode").value("TMP"))
                .andExpect(jsonPath("$[1].contractSubtypeCode").value("PT1"));

        ArgumentCaptor<ListEmployeeContractsCommand> captor =
                ArgumentCaptor.forClass(ListEmployeeContractsCommand.class);
        verify(listEmployeeContractsUseCase).listByEmployeeBusinessKey(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
    }

    @Test
    void getMapsPathToCommandAndReturns200() throws Exception {
        when(getContractByBusinessKeyUseCase.getByBusinessKey(any(GetContractByBusinessKeyCommand.class)))
                .thenReturn(contract("IND", "FT1", LocalDate.of(2026, 1, 1), null));

        mockMvc.perform(get("/employees/ESP/INTERNAL/EMP001/contracts/2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractCode").value("IND"))
                .andExpect(jsonPath("$.contractSubtypeCode").value("FT1"));

        ArgumentCaptor<GetContractByBusinessKeyCommand> captor =
                ArgumentCaptor.forClass(GetContractByBusinessKeyCommand.class);
        verify(getContractByBusinessKeyUseCase).getByBusinessKey(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(LocalDate.of(2026, 1, 1), captor.getValue().startDate());
    }

    @Test
    void updateMapsPathAndBodyToCommandAndReturns200() throws Exception {
        when(updateContractUseCase.update(any(UpdateContractCommand.class)))
                .thenReturn(contract("TMP", "INT", LocalDate.of(2026, 1, 1), null));

        mockMvc.perform(put("/employees/ESP/INTERNAL/EMP001/contracts/2026-01-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractCode": "TMP",
                                  "contractSubtypeCode": "INT"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractCode").value("TMP"))
                .andExpect(jsonPath("$.contractSubtypeCode").value("INT"));

        ArgumentCaptor<UpdateContractCommand> captor = ArgumentCaptor.forClass(UpdateContractCommand.class);
        verify(updateContractUseCase).update(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(LocalDate.of(2026, 1, 1), captor.getValue().startDate());
        assertEquals("TMP", captor.getValue().contractCode());
        assertEquals("INT", captor.getValue().contractSubtypeCode());
    }

    @Test
    void closeEndpointUsesDomainActionPath() throws Exception {
        when(closeContractUseCase.close(any(CloseContractCommand.class)))
                .thenReturn(contract(
                        "IND",
                        "FT1",
                        LocalDate.of(2026, 1, 1),
                        LocalDate.of(2026, 1, 31)
                ));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/contracts/2026-01-01/close")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "endDate": "2026-01-31"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractCode").value("IND"));

        ArgumentCaptor<CloseContractCommand> captor =
                ArgumentCaptor.forClass(CloseContractCommand.class);
        verify(closeContractUseCase).close(captor.capture());
        assertEquals(LocalDate.of(2026, 1, 1), captor.getValue().startDate());
        assertEquals(LocalDate.of(2026, 1, 31), captor.getValue().endDate());
    }

    @Test
    void replaceFromDateMapsPathAndBodyToCommandAndReturns200() throws Exception {
        when(replaceContractFromDateUseCase.replaceFromDate(any(ReplaceContractFromDateCommand.class)))
                .thenReturn(contract("TMP", "PT1", LocalDate.of(2026, 3, 1), null));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/contracts/replace-from-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "effectiveDate": "2026-03-01",
                                  "contractCode": "TMP",
                                  "contractSubtypeCode": "PT1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contractCode").value("TMP"))
                .andExpect(jsonPath("$.contractSubtypeCode").value("PT1"))
                .andExpect(jsonPath("$.startDate[0]").value(2026))
                .andExpect(jsonPath("$.startDate[1]").value(3))
                .andExpect(jsonPath("$.startDate[2]").value(1))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.employeeId").doesNotExist());

        ArgumentCaptor<ReplaceContractFromDateCommand> captor =
                ArgumentCaptor.forClass(ReplaceContractFromDateCommand.class);
        verify(replaceContractFromDateUseCase).replaceFromDate(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(LocalDate.of(2026, 3, 1), captor.getValue().effectiveDate());
    }

    @Test
    void replaceFromDateMapsConflictToHttp409() throws Exception {
        when(replaceContractFromDateUseCase.replaceFromDate(any(ReplaceContractFromDateCommand.class)))
                .thenThrow(new ContractOverlapException(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        LocalDate.of(2026, 3, 1),
                        null
                ));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/contracts/replace-from-date")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "effectiveDate": "2026-03-01",
                                  "contractCode": "TMP",
                                  "contractSubtypeCode": "PT1"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("overlaps")));
    }

    @Test
    void mapsConflictToHttp409() throws Exception {
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenThrow(new ContractOverlapException(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        LocalDate.of(2026, 1, 1),
                        null
                ));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractCode": "IND",
                                  "contractSubtypeCode": "FT1",
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("overlaps")));
    }

    @Test
    void mapsBadRequestToHttp400() throws Exception {
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenThrow(new ContractInvalidException("BAD"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractCode": "BAD",
                                  "contractSubtypeCode": "FT1",
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("contractCode")));
    }

    @Test
    void mapsInvalidContractCodeLengthToHttp400() throws Exception {
        when(createContractUseCase.create(any(CreateContractCommand.class)))
                .thenThrow(new ContractInvalidException("AB"));

        mockMvc.perform(post("/employees/ESP/INTERNAL/EMP001/contracts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractCode": "AB",
                                  "contractSubtypeCode": "FT1",
                                  "startDate": "2026-01-01"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("contractCode")));
    }

    @Test
    void mapsInvalidContractSubtypeCodeLengthToHttp400() throws Exception {
        when(updateContractUseCase.update(any(UpdateContractCommand.class)))
                .thenThrow(new ContractSubtypeInvalidException("ABCD"));

        mockMvc.perform(put("/employees/ESP/INTERNAL/EMP001/contracts/2026-01-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "contractCode": "IND",
                                  "contractSubtypeCode": "ABCD"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("contractSubtypeCode")));
    }

    @Test
    void mapsNotFoundToHttp404() throws Exception {
        when(getContractByBusinessKeyUseCase.getByBusinessKey(any()))
                .thenThrow(new ContractNotFoundException(
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        LocalDate.of(2026, 1, 1)
                ));

        mockMvc.perform(get("/employees/ESP/INTERNAL/EMP001/contracts/2026-01-01"))
                .andExpect(status().isNotFound());
    }

    @Test
    void doesNotExposeAlternateIdRoute() throws Exception {
        mockMvc.perform(get("/employees/ESP/INTERNAL/EMP001/contracts/IND/2026-01-01"))
                .andExpect(status().isNotFound());
    }

    private Contract contract(
            String contractCode,
            String contractSubtypeCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new Contract(
                10L,
                contractCode,
                contractSubtypeCode,
                startDate,
                endDate
        );
    }
}
