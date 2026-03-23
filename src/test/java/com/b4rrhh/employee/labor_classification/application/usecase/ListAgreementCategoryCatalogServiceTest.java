package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ListAgreementCategoryCatalogCommand;
import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;
import com.b4rrhh.employee.labor_classification.application.port.AgreementCategoryCatalogLookupPort;
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
class ListAgreementCategoryCatalogServiceTest {

    @Mock
    private AgreementCategoryCatalogLookupPort agreementCategoryCatalogLookupPort;

    private ListAgreementCategoryCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ListAgreementCategoryCatalogService(agreementCategoryCatalogLookupPort);
    }

    @Test
    void usesQueryWithoutReferenceDateWhenReferenceDateNotProvided() {
        when(agreementCategoryCatalogLookupPort.listActiveCategoriesByAgreement(
                eq("ESP"),
                eq("AGR_01")
        )).thenReturn(List.of(
                new AgreementCategoryCatalogItem("CAT_A", "Category A", LocalDate.of(2020, 1, 1), null)
        ));

        List<AgreementCategoryCatalogItem> result = service.list(new ListAgreementCategoryCatalogCommand(
                " esp ",
                " agr_01 ",
                null
        ));

        assertEquals(1, result.size());
        assertEquals("CAT_A", result.get(0).code());
        verify(agreementCategoryCatalogLookupPort).listActiveCategoriesByAgreement("ESP", "AGR_01");
    }

    @Test
    void usesQueryWithReferenceDateWhenReferenceDateProvided() {
        LocalDate referenceDate = LocalDate.of(2026, 3, 1);
        when(agreementCategoryCatalogLookupPort.listActiveCategoriesByAgreementOnDate(
                eq("ESP"),
                eq("AGR_01"),
                eq(referenceDate)
        )).thenReturn(List.of(
                new AgreementCategoryCatalogItem("CAT_B", "Category B", LocalDate.of(2020, 1, 1), null)
        ));

        List<AgreementCategoryCatalogItem> result = service.list(new ListAgreementCategoryCatalogCommand(
                "esp",
                "agr_01",
                referenceDate
        ));

        assertEquals(1, result.size());
        assertEquals("CAT_B", result.get(0).code());
        verify(agreementCategoryCatalogLookupPort)
                .listActiveCategoriesByAgreementOnDate("ESP", "AGR_01", referenceDate);
    }
}