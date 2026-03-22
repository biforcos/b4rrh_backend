package com.b4rrhh.employee.address.infrastructure.web.assembler;

import com.b4rrhh.employee.address.application.port.AddressCatalogReadPort;
import com.b4rrhh.employee.address.domain.model.Address;
import com.b4rrhh.employee.address.infrastructure.web.dto.AddressResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddressResponseAssembler {

    private final AddressCatalogReadPort addressCatalogReadPort;

    public AddressResponseAssembler(AddressCatalogReadPort addressCatalogReadPort) {
        this.addressCatalogReadPort = addressCatalogReadPort;
    }

    public AddressResponse toResponse(String ruleSystemCode, Address address) {
        String addressTypeName = addressCatalogReadPort
                .findAddressTypeName(ruleSystemCode, address.getAddressTypeCode())
                .orElse(null);

        return new AddressResponse(
                address.getAddressNumber(),
                address.getAddressTypeCode(),
                addressTypeName,
                address.getStreet(),
                address.getCity(),
                address.getCountryCode(),
                address.getPostalCode(),
                address.getRegionCode(),
                address.getStartDate(),
                address.getEndDate()
        );
    }

    public List<AddressResponse> toResponseList(String ruleSystemCode, List<Address> addresses) {
        return addresses.stream()
                .map(address -> toResponse(ruleSystemCode, address))
                .toList();
    }
}
