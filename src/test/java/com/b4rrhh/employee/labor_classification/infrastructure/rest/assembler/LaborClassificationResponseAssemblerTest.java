package com.b4rrhh.employee.labor_classification.infrastructure.rest.assembler;

import com.b4rrhh.employee.labor_classification.application.port.LaborClassificationCatalogReadPort;
import com.b4rrhh.employee.labor_classification.domain.model.LaborClassification;
import com.b4rrhh.employee.labor_classification.infrastructure.rest.dto.LaborClassificationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LaborClassificationResponseAssemblerTest {

    @Mock
    private LaborClassificationCatalogReadPort laborClassificationCatalogReadPort;

    @InjectMocks
    private LaborClassificationResponseAssembler assembler;

    @Test
    void toResponseEnrichesBothLabelsWhenPresent() {
        LaborClassification laborClassification = laborClassification(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );
        when(laborClassificationCatalogReadPort.findAgreementName("ESP", "AGR_OFFICE"))
                .thenReturn(Optional.of("Office Agreement"));
        when(laborClassificationCatalogReadPort.findAgreementCategoryName("ESP", "CAT_ADMIN"))
                .thenReturn(Optional.of("Administrative Category"));

        LaborClassificationResponse response = assembler.toResponse("ESP", laborClassification);

        assertEquals("AGR_OFFICE", response.agreementCode());
        assertEquals("Office Agreement", response.agreementName());
        assertEquals("CAT_ADMIN", response.agreementCategoryCode());
        assertEquals("Administrative Category", response.agreementCategoryName());
    }

    @Test
    void toResponseKeepsCodesAndUsesNullWhenLabelsMissing() {
        LaborClassification laborClassification = laborClassification(
                "AGR_OFFICE",
                "CAT_ADMIN",
                LocalDate.of(2026, 1, 1),
                null
        );
        when(laborClassificationCatalogReadPort.findAgreementName("ESP", "AGR_OFFICE"))
                .thenReturn(Optional.empty());
        when(laborClassificationCatalogReadPort.findAgreementCategoryName("ESP", "CAT_ADMIN"))
                .thenReturn(Optional.empty());

        LaborClassificationResponse response = assembler.toResponse("ESP", laborClassification);

        assertEquals("AGR_OFFICE", response.agreementCode());
        assertEquals("CAT_ADMIN", response.agreementCategoryCode());
        assertNull(response.agreementName());
        assertNull(response.agreementCategoryName());
    }

    private LaborClassification laborClassification(
            String agreementCode,
            String agreementCategoryCode,
            LocalDate startDate,
            LocalDate endDate
    ) {
        return new LaborClassification(
                10L,
                agreementCode,
                agreementCategoryCode,
                startDate,
                endDate
        );
    }
}