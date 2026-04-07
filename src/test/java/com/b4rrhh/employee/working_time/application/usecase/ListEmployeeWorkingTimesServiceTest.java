package com.b4rrhh.employee.working_time.application.usecase;

import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeContext;
import com.b4rrhh.employee.working_time.application.port.EmployeeWorkingTimeLookupPort;
import com.b4rrhh.employee.working_time.application.service.FixedWorkingTimeDerivationPolicy;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListEmployeeWorkingTimesServiceTest {

    private static final FixedWorkingTimeDerivationPolicy DERIVATION_POLICY = new FixedWorkingTimeDerivationPolicy();

    @Mock
    private WorkingTimeRepository workingTimeRepository;
    @Mock
    private EmployeeWorkingTimeLookupPort employeeWorkingTimeLookupPort;

    private ListEmployeeWorkingTimesService service;

    @BeforeEach
    void setUp() {
        service = new ListEmployeeWorkingTimesService(workingTimeRepository, employeeWorkingTimeLookupPort);
    }

    @Test
    void listsWorkingTimesByEmployeeBusinessKey() {
        when(employeeWorkingTimeLookupPort.findByBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(Optional.of(new EmployeeWorkingTimeContext(10L, "ESP", "INTERNAL", "EMP001")));
        when(workingTimeRepository.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                workingTime(1, LocalDate.of(2026, 1, 1)),
                workingTime(2, LocalDate.of(2026, 3, 1))
        ));

        List<WorkingTime> result = service.listByEmployeeBusinessKey(
                new ListEmployeeWorkingTimesCommand("ESP", "INTERNAL", "EMP001")
        );

        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getWorkingTimeNumber());
        assertEquals(2, result.get(1).getWorkingTimeNumber());
    }

    private WorkingTime workingTime(int number, LocalDate startDate) {
        BigDecimal percentage = new BigDecimal("100");
        WorkingTimeDerivedHours derivedHours = DERIVATION_POLICY.derive(percentage);

        return WorkingTime.rehydrate(
                (long) number,
                10L,
                number,
                startDate,
                null,
                percentage,
                derivedHours,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}