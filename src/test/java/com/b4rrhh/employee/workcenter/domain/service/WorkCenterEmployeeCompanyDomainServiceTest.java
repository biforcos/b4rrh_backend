package com.b4rrhh.employee.workcenter.domain.service;

import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCatalogValueInvalidException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterCompanyMismatchException;
import com.b4rrhh.employee.workcenter.domain.exception.WorkCenterOutsidePresencePeriodException;
import com.b4rrhh.employee.workcenter.domain.port.EmployeeActiveCompanyLookupPort;
import com.b4rrhh.employee.workcenter.domain.port.WorkCenterCompanyLookupPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkCenterEmployeeCompanyDomainServiceTest {

    @Mock
    private EmployeeActiveCompanyLookupPort employeeActiveCompanyLookupPort;
    @Mock
    private WorkCenterCompanyLookupPort workCenterCompanyLookupPort;

    private WorkCenterEmployeeCompanyDomainService service;

    @BeforeEach
    void setUp() {
        service = new WorkCenterEmployeeCompanyDomainService(
                employeeActiveCompanyLookupPort,
                workCenterCompanyLookupPort
        );
    }

    @Test
    void rejectsWhenEmployeeHasNoActiveCompanyAtReferenceDate() {
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 4, 15)))
                .thenReturn(Optional.empty());

        assertThrows(
                WorkCenterOutsidePresencePeriodException.class,
                () -> service.validateWorkCenterBelongsToEmployeeCompany(
                        10L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "MADRID_01",
                        LocalDate.of(2026, 4, 15)
                )
        );
    }

    @Test
    void rejectsWhenWorkCenterDoesNotBelongToEmployeeCompany() {
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 4, 15)))
                .thenReturn(Optional.of("ES01"));
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "MADRID_01", LocalDate.of(2026, 4, 15)))
                .thenReturn(Optional.of("ES99"));

        assertThrows(
                WorkCenterCompanyMismatchException.class,
                () -> service.validateWorkCenterBelongsToEmployeeCompany(
                        10L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "MADRID_01",
                        LocalDate.of(2026, 4, 15)
                )
        );
    }

    @Test
    void rejectsWhenWorkCenterCatalogEntryDoesNotResolveCompany() {
        when(employeeActiveCompanyLookupPort.findActiveCompanyCode(10L, LocalDate.of(2026, 4, 15)))
                .thenReturn(Optional.of("ES01"));
        when(workCenterCompanyLookupPort.findCompanyCode("ESP", "MADRID_01", LocalDate.of(2026, 4, 15)))
                .thenReturn(Optional.empty());

        assertThrows(
                WorkCenterCatalogValueInvalidException.class,
                () -> service.validateWorkCenterBelongsToEmployeeCompany(
                        10L,
                        "ESP",
                        "INTERNAL",
                        "EMP001",
                        "MADRID_01",
                        LocalDate.of(2026, 4, 15)
                )
        );
    }
}