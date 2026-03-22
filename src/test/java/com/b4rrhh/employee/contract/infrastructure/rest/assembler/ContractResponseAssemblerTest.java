package com.b4rrhh.employee.contract.infrastructure.rest.assembler;

import com.b4rrhh.employee.contract.application.port.ContractCatalogReadPort;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.infrastructure.rest.dto.ContractResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContractResponseAssemblerTest {

    @Mock
    private ContractCatalogReadPort contractCatalogReadPort;

    @Test
    void toResponseEnrichesContractTypeAndSubtypeNamesWhenPresent() {
        ContractResponseAssembler assembler = new ContractResponseAssembler(contractCatalogReadPort);
        Contract contract = contract("IND", "FT1");
        when(contractCatalogReadPort.findContractTypeName("ESP", "IND"))
                .thenReturn(Optional.of("Indefinido"));
        when(contractCatalogReadPort.findContractSubtypeName("ESP", "FT1"))
                .thenReturn(Optional.of("Tiempo completo"));

        ContractResponse response = assembler.toResponse("ESP", contract);

        assertEquals("IND", response.contractCode());
        assertEquals("Indefinido", response.contractTypeName());
        assertEquals("FT1", response.contractSubtypeCode());
        assertEquals("Tiempo completo", response.contractSubtypeName());
    }

    @Test
    void toResponseKeepsCodesAndUsesNullWhenLabelsAreMissing() {
        ContractResponseAssembler assembler = new ContractResponseAssembler(contractCatalogReadPort);
        Contract contract = contract("TMP", "PT1");
        when(contractCatalogReadPort.findContractTypeName("ESP", "TMP"))
                .thenReturn(Optional.empty());
        when(contractCatalogReadPort.findContractSubtypeName("ESP", "PT1"))
                .thenReturn(Optional.empty());

        ContractResponse response = assembler.toResponse("ESP", contract);

        assertEquals("TMP", response.contractCode());
        assertNull(response.contractTypeName());
        assertEquals("PT1", response.contractSubtypeCode());
        assertNull(response.contractSubtypeName());
    }

    private Contract contract(String contractCode, String contractSubtypeCode) {
        return new Contract(
                                10L,
                contractCode,
                contractSubtypeCode,
                LocalDate.of(2026, 1, 10),
                                null
        );
    }
}
