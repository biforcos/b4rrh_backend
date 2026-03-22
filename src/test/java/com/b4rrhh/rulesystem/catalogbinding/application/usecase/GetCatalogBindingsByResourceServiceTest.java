package com.b4rrhh.rulesystem.catalogbinding.application.usecase;

import com.b4rrhh.rulesystem.catalogbinding.application.query.GetCatalogBindingsByResourceQuery;
import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogFieldBinding;
import com.b4rrhh.rulesystem.catalogbinding.domain.model.CatalogKind;
import com.b4rrhh.rulesystem.catalogbinding.domain.port.CatalogBindingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetCatalogBindingsByResourceServiceTest {

    @Mock
    private CatalogBindingRepository catalogBindingRepository;

    private GetCatalogBindingsByResourceService service;

    @BeforeEach
    void setUp() {
        service = new GetCatalogBindingsByResourceService(catalogBindingRepository);
    }

    @Test
        void returnsFieldsForResourceCode() {
        when(catalogBindingRepository.findActiveByResourceCode("employee.work_center"))
                .thenReturn(List.of(new CatalogFieldBinding(
                        "employee.work_center",
                        "workCenterCode",
                        CatalogKind.DIRECT,
                        "WORK_CENTER",
                        null,
                        null,
                        true
                )));

        List<CatalogFieldBinding> result = service.getByResourceCode(
                new GetCatalogBindingsByResourceQuery("employee.work_center")
        );

        assertEquals(1, result.size());
        assertEquals("workCenterCode", result.get(0).fieldCode());
        verify(catalogBindingRepository).findActiveByResourceCode("employee.work_center");
    }

    @Test
        void returnsEmptyWhenResourceHasNoFields() {
        when(catalogBindingRepository.findActiveByResourceCode("employee.unknown"))
                .thenReturn(List.of());

        List<CatalogFieldBinding> result = service.getByResourceCode(
                new GetCatalogBindingsByResourceQuery("employee.unknown")
        );

        assertEquals(0, result.size());
    }

    @Test
    void rejectsBlankResourceCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getByResourceCode(new GetCatalogBindingsByResourceQuery("  "))
        );
    }
}
