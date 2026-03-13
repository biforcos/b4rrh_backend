package com.b4rrhh.employee.address.infrastructure.web;

import com.b4rrhh.employee.address.application.usecase.CloseAddressCommand;
import com.b4rrhh.employee.address.application.usecase.CloseAddressUseCase;
import com.b4rrhh.employee.address.application.usecase.CreateAddressCommand;
import com.b4rrhh.employee.address.application.usecase.CreateAddressUseCase;
import com.b4rrhh.employee.address.application.usecase.GetAddressByBusinessKeyUseCase;
import com.b4rrhh.employee.address.application.usecase.ListEmployeeAddressesUseCase;
import com.b4rrhh.employee.address.domain.model.Address;
import com.b4rrhh.employee.address.infrastructure.web.dto.AddressResponse;
import com.b4rrhh.employee.address.infrastructure.web.dto.CloseAddressRequest;
import com.b4rrhh.employee.address.infrastructure.web.dto.CreateAddressRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/addresses")
public class AddressBusinessKeyController {

    private final CreateAddressUseCase createAddressUseCase;
    private final CloseAddressUseCase closeAddressUseCase;
    private final GetAddressByBusinessKeyUseCase getAddressByBusinessKeyUseCase;
    private final ListEmployeeAddressesUseCase listEmployeeAddressesUseCase;

    public AddressBusinessKeyController(
            CreateAddressUseCase createAddressUseCase,
            CloseAddressUseCase closeAddressUseCase,
            GetAddressByBusinessKeyUseCase getAddressByBusinessKeyUseCase,
            ListEmployeeAddressesUseCase listEmployeeAddressesUseCase
    ) {
        this.createAddressUseCase = createAddressUseCase;
        this.closeAddressUseCase = closeAddressUseCase;
        this.getAddressByBusinessKeyUseCase = getAddressByBusinessKeyUseCase;
        this.listEmployeeAddressesUseCase = listEmployeeAddressesUseCase;
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody CreateAddressRequest request
    ) {
        Address created = createAddressUseCase.create(
                new CreateAddressCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        request.addressTypeCode(),
                        request.street(),
                        request.city(),
                        request.countryCode(),
                        request.postalCode(),
                        request.regionCode(),
                        request.startDate(),
                        request.endDate()
                )
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(created));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> list(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        List<AddressResponse> response = listEmployeeAddressesUseCase
                .listByEmployeeBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .stream()
                .map(this::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{addressNumber}")
    public ResponseEntity<AddressResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer addressNumber
    ) {
        return getAddressByBusinessKeyUseCase.getByBusinessKey(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        addressNumber
                )
                .map(address -> ResponseEntity.ok(toResponse(address)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{addressNumber}/close")
    public ResponseEntity<AddressResponse> close(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable Integer addressNumber,
            @RequestBody CloseAddressRequest request
    ) {
        Address closed = closeAddressUseCase.close(
                new CloseAddressCommand(
                        ruleSystemCode,
                        employeeTypeCode,
                        employeeNumber,
                        addressNumber,
                        request.endDate()
                )
        );

        return ResponseEntity.ok(toResponse(closed));
    }

    private AddressResponse toResponse(Address address) {
        return new AddressResponse(
                address.getAddressNumber(),
                address.getAddressTypeCode(),
                address.getStreet(),
                address.getCity(),
                address.getCountryCode(),
                address.getPostalCode(),
                address.getRegionCode(),
                address.getStartDate(),
                address.getEndDate()
        );
    }
}
