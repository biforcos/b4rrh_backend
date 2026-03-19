package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ListEmployeeContractsCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.domain.port.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmployeeContractsServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private EmployeeContractLookupPort employeeContractLookupPort;

    private ListEmployeeContractsService service;

    @BeforeEach
    void setUp() {
        service = new ListEmployeeContractsService(contractRepository, employeeContractLookupPort);
    }

    @Test
    void listsContractsByEmployeeBusinessKey() {
        when(employeeContractLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(contractRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                contract("IND", "FT1", LocalDate.of(2026, 1, 1), null),
                contract("TMP", "PT1", LocalDate.of(2026, 2, 1), null)
        ));

        List<Contract> result = service.listByEmployeeBusinessKey(
                new ListEmployeeContractsCommand(" esp ", " internal ", " EMP001 ")
        );

        assertEquals(2, result.size());
        assertEquals("IND", result.get(0).getContractCode());
        assertEquals("PT1", result.get(1).getContractSubtypeCode());
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeContractLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                ContractEmployeeNotFoundException.class,
                () -> service.listByEmployeeBusinessKey(
                        new ListEmployeeContractsCommand(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER)
                )
        );
    }

    private EmployeeContractContext employeeContext(Long employeeId) {
        return new EmployeeContractContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
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
