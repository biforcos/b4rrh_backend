package com.b4rrhh.employee.workcenter.infrastructure.web.assembler;

import com.b4rrhh.employee.workcenter.application.port.WorkCenterCatalogReadPort;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.infrastructure.web.dto.WorkCenterResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkCenterResponseAssemblerTest {

    @Mock
    private WorkCenterCatalogReadPort workCenterCatalogReadPort;

    @Test
    void toResponseEnrichesLabelWhenPresent() {
        WorkCenterResponseAssembler assembler = new WorkCenterResponseAssembler(workCenterCatalogReadPort);
        WorkCenter workCenter = workCenter(1, "MADRID_HQ", LocalDate.of(2026, 1, 10), null);
        when(workCenterCatalogReadPort.findWorkCenterName("ESP", "MADRID_HQ"))
                .thenReturn(Optional.of("Oficina central"));
        when(workCenterCatalogReadPort.findWorkCenterCompanyCode("ESP", "MADRID_HQ", LocalDate.of(2026, 1, 10)))
            .thenReturn(Optional.of("COMP"));
        when(workCenterCatalogReadPort.findCompanyName("ESP", "COMP"))
            .thenReturn(Optional.of("Compañía principal"));

        WorkCenterResponse response = assembler.toResponse("ESP", workCenter);

        assertEquals(1, response.workCenterAssignmentNumber());
        assertEquals("MADRID_HQ", response.workCenterCode());
        assertEquals("Oficina central", response.workCenterName());
        assertEquals("COMP", response.companyCode());
        assertEquals("Compañía principal", response.companyName());
    }

    @Test
    void toResponseKeepsCodeAndUsesNullWhenLabelMissing() {
        WorkCenterResponseAssembler assembler = new WorkCenterResponseAssembler(workCenterCatalogReadPort);
        WorkCenter workCenter = workCenter(1, "MADRID_HQ", LocalDate.of(2026, 1, 10), null);
        when(workCenterCatalogReadPort.findWorkCenterName("ESP", "MADRID_HQ"))
                .thenReturn(Optional.empty());
        when(workCenterCatalogReadPort.findWorkCenterCompanyCode("ESP", "MADRID_HQ", LocalDate.of(2026, 1, 10)))
            .thenReturn(Optional.empty());

        WorkCenterResponse response = assembler.toResponse("ESP", workCenter);

        assertEquals("MADRID_HQ", response.workCenterCode());
        assertNull(response.workCenterName());
        assertNull(response.companyCode());
        assertNull(response.companyName());
    }

    private WorkCenter workCenter(
            int assignmentNumber,
            String workCenterCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new WorkCenter(
                1L,
                10L,
                assignmentNumber,
                workCenterCode,
                startDate,
                endDate,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}