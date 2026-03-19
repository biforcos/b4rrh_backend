package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.GetContractByBusinessKeyCommand;
import com.b4rrhh.employee.contract.application.port.EmployeeContractContext;
import com.b4rrhh.employee.contract.application.port.EmployeeContractLookupPort;
import com.b4rrhh.employee.contract.domain.exception.ContractEmployeeNotFoundException;
import com.b4rrhh.employee.contract.domain.exception.ContractNotFoundException;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.domain.port.ContractRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetContractByBusinessKeyServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private ContractRepository contractRepository;
    @Mock
    private EmployeeContractLookupPort employeeContractLookupPort;

    private GetContractByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new GetContractByBusinessKeyService(contractRepository, employeeContractLookupPort);
    }

    @Test
    void getsContractByBusinessKey() {
        Contract expected = contract("IND", "FT1", LocalDate.of(2026, 1, 1), null);

        when(employeeContractLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(contractRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.of(expected));

        Contract result = service.getByBusinessKey(new GetContractByBusinessKeyCommand(
                " esp ",
                " internal ",
                " EMP001 ",
                LocalDate.of(2026, 1, 1)
        ));

        assertEquals("IND", result.getContractCode());
        assertEquals("FT1", result.getContractSubtypeCode());
    }

    @Test
    void throwsWhenContractDoesNotExist() {
        when(employeeContractLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(contractRepository.findByEmployeeIdAndStartDate(10L, LocalDate.of(2026, 1, 1)))
                .thenReturn(Optional.empty());

        assertThrows(
                ContractNotFoundException.class,
                () -> service.getByBusinessKey(new GetContractByBusinessKeyCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        LocalDate.of(2026, 1, 1)
                ))
        );
    }

    @Test
    void throwsWhenEmployeeBusinessKeyDoesNotExist() {
        when(employeeContractLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                ContractEmployeeNotFoundException.class,
                () -> service.getByBusinessKey(new GetContractByBusinessKeyCommand(
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        LocalDate.of(2026, 1, 1)
                ))
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
