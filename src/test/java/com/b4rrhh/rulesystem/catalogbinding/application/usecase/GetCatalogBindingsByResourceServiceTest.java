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
                        CatalogKind.CUSTOM,
                        null,
                        null,
                        "WORK_CENTER_BY_COMPANY",
                        true
                )));

        List<CatalogFieldBinding> result = service.getByResourceCode(
                new GetCatalogBindingsByResourceQuery("employee.work_center")
        );

        assertEquals(1, result.size());
        assertEquals("workCenterCode", result.get(0).fieldCode());
                assertEquals(CatalogKind.CUSTOM, result.get(0).catalogKind());
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
    void returnsContractTypeAndSubtypeBindingsForEmployeeContractResource() {
        when(catalogBindingRepository.findActiveByResourceCode("employee.contract"))
                .thenReturn(List.of(
                        new CatalogFieldBinding(
                                "employee.contract",
                                "contractTypeCode",
                                CatalogKind.DIRECT,
                                "CONTRACT",
                                null,
                                null,
                                true
                        ),
                        new CatalogFieldBinding(
                                "employee.contract",
                                "contractSubtypeCode",
                                CatalogKind.DEPENDENT,
                                "CONTRACT_SUBTYPE",
                                "contractTypeCode",
                                null,
                                true
                        )
                ));

        List<CatalogFieldBinding> result = service.getByResourceCode(
                new GetCatalogBindingsByResourceQuery("employee.contract")
        );

        assertEquals(2, result.size());
        assertEquals("contractTypeCode", result.get(0).fieldCode());
        assertEquals(CatalogKind.DIRECT, result.get(0).catalogKind());
        assertEquals("contractSubtypeCode", result.get(1).fieldCode());
        assertEquals(CatalogKind.DEPENDENT, result.get(1).catalogKind());
        assertEquals("contractTypeCode", result.get(1).dependsOnFieldCode());
        verify(catalogBindingRepository).findActiveByResourceCode("employee.contract");
    }

    @Test
    void rejectsBlankResourceCode() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.getByResourceCode(new GetCatalogBindingsByResourceQuery("  "))
        );
    }
}
