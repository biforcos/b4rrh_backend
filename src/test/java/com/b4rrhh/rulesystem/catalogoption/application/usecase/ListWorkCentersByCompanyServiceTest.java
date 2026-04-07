package com.b4rrhh.rulesystem.catalogoption.application.usecase;

import com.b4rrhh.rulesystem.catalogoption.application.query.ListWorkCentersByCompanyQuery;
import com.b4rrhh.rulesystem.catalogoption.domain.model.WorkCenterByCompanyOption;
import com.b4rrhh.rulesystem.catalogoption.domain.port.WorkCenterByCompanyCatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListWorkCentersByCompanyServiceTest {

    @Mock
    private WorkCenterByCompanyCatalogRepository workCenterByCompanyCatalogRepository;

    private ListWorkCentersByCompanyService service;

    @BeforeEach
    void setUp() {
        service = new ListWorkCentersByCompanyService(workCenterByCompanyCatalogRepository);
    }

    @Test
    void returnsFilteredWorkCentersForCompany() {
        when(workCenterByCompanyCatalogRepository.findByCompany(
                "ESP",
                "COMP",
                LocalDate.of(2026, 4, 15),
                "%madrid%"
        )).thenReturn(List.of(new WorkCenterByCompanyOption("MADRID_01", "Madrid 01")));

        WorkCentersByCompanyResult result = service.get(new ListWorkCentersByCompanyQuery(
                "esp",
                "comp",
                LocalDate.of(2026, 4, 15),
                " madrid "
        ));

        assertEquals("ESP", result.ruleSystemCode());
        assertEquals("COMP", result.companyCode());
        assertEquals(LocalDate.of(2026, 4, 15), result.referenceDate());
        assertEquals(1, result.items().size());
        verify(workCenterByCompanyCatalogRepository).findByCompany("ESP", "COMP", LocalDate.of(2026, 4, 15), "%madrid%");
    }

    @Test
    void rejectsBlankCompanyCode() {
        assertThrows(IllegalArgumentException.class, () -> service.get(new ListWorkCentersByCompanyQuery(
                "ESP",
                "  ",
                null,
                null
        )));
    }
}
