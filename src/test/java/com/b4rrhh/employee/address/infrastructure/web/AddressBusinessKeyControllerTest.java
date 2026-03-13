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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressBusinessKeyControllerTest {

    @Mock
    private CreateAddressUseCase createAddressUseCase;
    @Mock
    private CloseAddressUseCase closeAddressUseCase;
    @Mock
    private GetAddressByBusinessKeyUseCase getAddressByBusinessKeyUseCase;
    @Mock
    private ListEmployeeAddressesUseCase listEmployeeAddressesUseCase;

    private AddressBusinessKeyController controller;

    @BeforeEach
    void setUp() {
        controller = new AddressBusinessKeyController(
                createAddressUseCase,
                closeAddressUseCase,
                getAddressByBusinessKeyUseCase,
                listEmployeeAddressesUseCase
        );
    }

    @Test
    void createsAddressUsingEmployeeBusinessKey() {
        CreateAddressRequest request = new CreateAddressRequest(
                "HOME",
                "Calle Mayor 10",
                "Madrid",
                "ESP",
                "28013",
                "MD",
                LocalDate.of(2026, 1, 10),
                null
        );

        when(createAddressUseCase.create(any(CreateAddressCommand.class))).thenReturn(activeAddress());

        ResponseEntity<AddressResponse> response = controller.create("ESP", "INTERNAL", "EMP001", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().addressNumber());

        ArgumentCaptor<CreateAddressCommand> captor = ArgumentCaptor.forClass(CreateAddressCommand.class);
        verify(createAddressUseCase).create(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals("HOME", captor.getValue().addressTypeCode());
    }

    @Test
    void listsAddressesUsingEmployeeBusinessKey() {
        when(listEmployeeAddressesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activeAddress()));

        ResponseEntity<List<AddressResponse>> response = controller.list("ESP", "INTERNAL", "EMP001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1, response.getBody().get(0).addressNumber());
    }

    @Test
    void getsAddressByBusinessKeyAndAddressNumber() {
        when(getAddressByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001", 1))
                .thenReturn(Optional.of(activeAddress()));

        ResponseEntity<AddressResponse> response = controller.getByBusinessKey("ESP", "INTERNAL", "EMP001", 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().addressNumber());
    }

    @Test
    void closesAddressUsingBusinessKeyAndAddressNumber() {
        CloseAddressRequest request = new CloseAddressRequest(LocalDate.of(2026, 2, 1));
        when(closeAddressUseCase.close(any(CloseAddressCommand.class))).thenReturn(closedAddress());

        ResponseEntity<AddressResponse> response = controller.close("ESP", "INTERNAL", "EMP001", 1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(LocalDate.of(2026, 2, 1), response.getBody().endDate());

        ArgumentCaptor<CloseAddressCommand> captor = ArgumentCaptor.forClass(CloseAddressCommand.class);
        verify(closeAddressUseCase).close(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(1, captor.getValue().addressNumber());
    }

    private Address activeAddress() {
        return new Address(
                20L,
                10L,
                1,
                "HOME",
                "Calle Mayor 10",
                "Madrid",
                "ESP",
                "28013",
                "MD",
                LocalDate.of(2026, 1, 10),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Address closedAddress() {
        return new Address(
                20L,
                10L,
                1,
                "HOME",
                "Calle Mayor 10",
                "Madrid",
                "ESP",
                "28013",
                "MD",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 2, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
