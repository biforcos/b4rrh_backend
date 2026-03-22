package com.b4rrhh.employee.address.infrastructure.web.assembler;

import com.b4rrhh.employee.address.application.port.AddressCatalogReadPort;
import com.b4rrhh.employee.address.domain.model.Address;
import com.b4rrhh.employee.address.infrastructure.web.dto.AddressResponse;
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
class AddressResponseAssemblerTest {

    @Mock
    private AddressCatalogReadPort addressCatalogReadPort;

    @Test
    void toResponseEnrichesLabelWhenPresent() {
        AddressResponseAssembler assembler = new AddressResponseAssembler(addressCatalogReadPort);
        Address address = address(1, "HOME");
        when(addressCatalogReadPort.findAddressTypeName("ESP", "HOME"))
                .thenReturn(Optional.of("Domicilio"));

        AddressResponse response = assembler.toResponse("ESP", address);

        assertEquals(1, response.addressNumber());
        assertEquals("HOME", response.addressTypeCode());
        assertEquals("Domicilio", response.addressTypeName());
    }

    @Test
    void toResponseKeepsCodeAndUsesNullWhenLabelMissing() {
        AddressResponseAssembler assembler = new AddressResponseAssembler(addressCatalogReadPort);
        Address address = address(1, "HOME");
        when(addressCatalogReadPort.findAddressTypeName("ESP", "HOME"))
                .thenReturn(Optional.empty());

        AddressResponse response = assembler.toResponse("ESP", address);

        assertEquals("HOME", response.addressTypeCode());
        assertNull(response.addressTypeName());
    }

    private Address address(int addressNumber, String addressTypeCode) {
        return new Address(
                10L,
                20L,
                addressNumber,
                addressTypeCode,
                "Calle Mayor 10",
                "Madrid",
                "ESP",
                "28013",
                "M",
                LocalDate.of(2026, 1, 10),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
