package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ListContractSubtypeCatalogCommand;
import com.b4rrhh.employee.contract.application.model.ContractSubtypeCatalogItem;
import com.b4rrhh.employee.contract.application.port.ContractSubtypeCatalogLookupPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListContractSubtypeCatalogServiceTest {

    @Mock
    private ContractSubtypeCatalogLookupPort contractSubtypeCatalogLookupPort;

    private ListContractSubtypeCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ListContractSubtypeCatalogService(contractSubtypeCatalogLookupPort);
    }

    @Test
    void usesQueryWithoutReferenceDateWhenReferenceDateNotProvided() {
        when(contractSubtypeCatalogLookupPort.listActiveSubtypesByContractType(
                eq("ESP"),
                eq("IND")
        )).thenReturn(List.of(
                new ContractSubtypeCatalogItem("FT1", "Full Time", LocalDate.of(2020, 1, 1), null)
        ));

        List<ContractSubtypeCatalogItem> result = service.list(new ListContractSubtypeCatalogCommand(
                " esp ",
                " ind ",
                null
        ));

        assertEquals(1, result.size());
        assertEquals("FT1", result.get(0).code());
        verify(contractSubtypeCatalogLookupPort).listActiveSubtypesByContractType("ESP", "IND");
    }

    @Test
    void usesQueryWithReferenceDateWhenReferenceDateProvided() {
        LocalDate referenceDate = LocalDate.of(2026, 3, 1);
        when(contractSubtypeCatalogLookupPort.listActiveSubtypesByContractTypeOnDate(
                eq("ESP"),
                eq("IND"),
                eq(referenceDate)
        )).thenReturn(List.of(
                new ContractSubtypeCatalogItem("PT1", "Part Time", LocalDate.of(2020, 1, 1), null)
        ));

        List<ContractSubtypeCatalogItem> result = service.list(new ListContractSubtypeCatalogCommand(
                "esp",
                "ind",
                referenceDate
        ));

        assertEquals(1, result.size());
        assertEquals("PT1", result.get(0).code());
        verify(contractSubtypeCatalogLookupPort).listActiveSubtypesByContractTypeOnDate("ESP", "IND", referenceDate);
    }
}