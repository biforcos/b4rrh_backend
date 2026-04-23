package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.StandardWorkingTimeDerivationPolicy;
import com.b4rrhh.employee.working_time.domain.model.WorkingTimeDerivedHours;
import com.b4rrhh.employee.working_time.domain.model.WorkingTime;
import com.b4rrhh.employee.working_time.domain.port.WorkingTimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetWorkingTimeByBusinessKeyServiceTest {

    private static final StandardWorkingTimeDerivationPolicy DERIVATION_POLICY = new StandardWorkingTimeDerivationPolicy();

    @Mock
    private WorkingTimeRepository workingTimeRepository;
    @Mock
    private EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;

    private GetWorkingTimeByBusinessKeyService service;

    @BeforeEach
    void setUp() {
        service = new GetWorkingTimeByBusinessKeyService(workingTimeRepository, employeeWorkingTimeLookupPort);
    }

    @Test
    void getsWorkingTimeByEmployeeBusinessKeyAndFunctionalNumber() {
        BigDecimal percentage = new BigDecimal("75");
        WorkingTimeDerivedHours derivedHours = DERIVATION_POLICY.derive(percentage, new java.math.BigDecimal("1736"));
        WorkingTime workingTime = WorkingTime.rehydrate(
                1L,
                10L,
                3,
                LocalDate.of(2026, 1, 1),
                null,
                percentage,
                derivedHours,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(employeeWorkingTimeLookupPort.findByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(new EmployeeWorkingTimeContext(10L, "ESP", "INTERNAL", "EMP001")));
        when(workingTimeRepository.findByEmployeeIdAndWorkingTimeNumber(10L, 3)).thenReturn(Optional.of(workingTime));

        WorkingTime result = service.getByBusinessKey(
                new GetWorkingTimeByBusinessKeyCommand("ESP", "INTERNAL", "EMP001", 3)
        );

        assertEquals(3, result.getWorkingTimeNumber());
        assertEquals(new BigDecimal("75"), result.getWorkingTimePercentage());
    }
}