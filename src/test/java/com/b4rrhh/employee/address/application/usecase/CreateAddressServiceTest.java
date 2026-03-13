package com.b4rrhh.employee.address.application.usecase;

import com.b4rrhh.employee.address.application.port.EmployeeAddressContext;
import com.b4rrhh.employee.address.application.port.EmployeeAddressLookupPort;
import com.b4rrhh.employee.address.application.service.AddressCatalogValidator;
import com.b4rrhh.employee.address.domain.exception.AddressCatalogValueInvalidException;
import com.b4rrhh.employee.address.domain.exception.AddressOverlapException;
import com.b4rrhh.employee.address.domain.exception.InvalidAddressDateRangeException;
import com.b4rrhh.employee.address.domain.model.Address;
import com.b4rrhh.employee.address.domain.port.AddressRepository;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateAddressServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private EmployeeAddressLookupPort employeeAddressLookupPort;
    @Mock
    private RuleSystemRepository ruleSystemRepository;
    private AddressCatalogValidator addressCatalogValidator;

    private CreateAddressService service;

    @BeforeEach
    void setUp() {
        addressCatalogValidator = new TestAddressCatalogValidator();
        service = new CreateAddressService(
                addressRepository,
                employeeAddressLookupPort,
                ruleSystemRepository,
                addressCatalogValidator
        );
    }

    @Test
    void createsAddressAndAssignsNextAddressNumber() {
        CreateAddressCommand command = new CreateAddressCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "home",
                "Calle Mayor 10",
                "Madrid",
                "esp",
                "28013",
                "md",
                LocalDate.of(2026, 1, 10),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(addressRepository.findMaxAddressNumberByEmployeeId(10L)).thenReturn(Optional.of(2));
        when(addressRepository.existsOverlappingPeriodByAddressType(
            10L,
            "HOME",
            LocalDate.of(2026, 1, 10),
            null
        )).thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> {
            Address input = invocation.getArgument(0);
            return new Address(
                    99L,
                    input.getEmployeeId(),
                    input.getAddressNumber(),
                    input.getAddressTypeCode(),
                    input.getStreet(),
                    input.getCity(),
                    input.getCountryCode(),
                    input.getPostalCode(),
                    input.getRegionCode(),
                    input.getStartDate(),
                    input.getEndDate(),
                    LocalDateTime.now(),
                    LocalDateTime.now()
            );
        });

        Address created = service.create(command);

        assertEquals(99L, created.getId());
        assertEquals(3, created.getAddressNumber());
        assertEquals("HOME", created.getAddressTypeCode());
        assertEquals("ESP", created.getCountryCode());

        ArgumentCaptor<Address> captor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(captor.capture());
        assertEquals(3, captor.getValue().getAddressNumber());

        InOrder inOrder = inOrder(employeeAddressLookupPort, addressRepository);
        inOrder.verify(employeeAddressLookupPort)
                .findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
        inOrder.verify(addressRepository).findMaxAddressNumberByEmployeeId(10L);
        inOrder.verify(addressRepository)
            .existsOverlappingPeriodByAddressType(10L, "HOME", LocalDate.of(2026, 1, 10), null);
        }

        @Test
        void createsAddressWithSameStartAndEndDate() {
        CreateAddressCommand command = new CreateAddressCommand(
            RULE_SYSTEM_CODE,
            EMPLOYEE_TYPE_CODE,
            EMPLOYEE_NUMBER,
            "HOME",
            "Calle Mayor 10",
            "Madrid",
            "ESP",
            "28013",
            "MD",
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 10)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
            .thenReturn(Optional.of(employeeContext(10L)));
        when(addressRepository.findMaxAddressNumberByEmployeeId(10L)).thenReturn(Optional.empty());
        when(addressRepository.existsOverlappingPeriodByAddressType(
            10L,
            "HOME",
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 10)
        )).thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address created = service.create(command);

        assertEquals(LocalDate.of(2026, 1, 10), created.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 10), created.getEndDate());
        }

        @Test
        void rejectsOverlappingPeriodForSameAddressType() {
        CreateAddressCommand command = new CreateAddressCommand(
            RULE_SYSTEM_CODE,
            EMPLOYEE_TYPE_CODE,
            EMPLOYEE_NUMBER,
            "HOME",
            "Calle Mayor 10",
            "Madrid",
            "ESP",
            null,
            null,
            LocalDate.of(2026, 1, 10),
            null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
            .thenReturn(Optional.of(employeeContext(10L)));
        when(addressRepository.findMaxAddressNumberByEmployeeId(10L)).thenReturn(Optional.of(1));
        when(addressRepository.existsOverlappingPeriodByAddressType(
            10L,
            "HOME",
            LocalDate.of(2026, 1, 10),
            null
        )).thenReturn(true);

        assertThrows(AddressOverlapException.class, () -> service.create(command));
        verify(addressRepository, never()).save(any(Address.class));
        }

        @Test
        void rejectsOverlappingHistoricalPeriodForSameAddressType() {
        CreateAddressCommand command = new CreateAddressCommand(
            RULE_SYSTEM_CODE,
            EMPLOYEE_TYPE_CODE,
            EMPLOYEE_NUMBER,
            "HOME",
            "Calle Mayor 10",
            "Madrid",
            "ESP",
            null,
            null,
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 20)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
            .thenReturn(Optional.of(employeeContext(10L)));
        when(addressRepository.findMaxAddressNumberByEmployeeId(10L)).thenReturn(Optional.of(1));
        when(addressRepository.existsOverlappingPeriodByAddressType(
            10L,
            "HOME",
            LocalDate.of(2026, 1, 10),
            LocalDate.of(2026, 1, 20)
        )).thenReturn(true);

        assertThrows(AddressOverlapException.class, () -> service.create(command));
        verify(addressRepository, never()).save(any(Address.class));
        }

        @Test
        void allowsCreateForDifferentAddressType() {
        CreateAddressCommand command = new CreateAddressCommand(
            RULE_SYSTEM_CODE,
            EMPLOYEE_TYPE_CODE,
            EMPLOYEE_NUMBER,
            "MAILING",
            "Apartado 123",
            "Madrid",
            "ESP",
            "28013",
            "MD",
            LocalDate.of(2026, 1, 10),
            null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
            .thenReturn(Optional.of(employeeContext(10L)));
        when(addressRepository.findMaxAddressNumberByEmployeeId(10L)).thenReturn(Optional.of(3));
        when(addressRepository.existsOverlappingPeriodByAddressType(
            10L,
            "MAILING",
            LocalDate.of(2026, 1, 10),
            null
        )).thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address created = service.create(command);

        assertEquals("MAILING", created.getAddressTypeCode());
        assertEquals(4, created.getAddressNumber());
        }

        @Test
        void allowsSameAddressTypeAfterPreviousPeriodClosedWithoutOverlap() {
        CreateAddressCommand command = new CreateAddressCommand(
            RULE_SYSTEM_CODE,
            EMPLOYEE_TYPE_CODE,
            EMPLOYEE_NUMBER,
            "HOME",
            "Calle Mayor 10",
            "Madrid",
            "ESP",
            "28013",
            "MD",
            LocalDate.of(2026, 2, 1),
            null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
            .thenReturn(Optional.of(employeeContext(10L)));
        when(addressRepository.findMaxAddressNumberByEmployeeId(10L)).thenReturn(Optional.of(4));
        when(addressRepository.existsOverlappingPeriodByAddressType(
            10L,
            "HOME",
            LocalDate.of(2026, 2, 1),
            null
        )).thenReturn(false);
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Address created = service.create(command);

        assertEquals("HOME", created.getAddressTypeCode());
        assertEquals(5, created.getAddressNumber());
    }

    @Test
    void rejectsInvalidCatalogValue() {
        CreateAddressCommand command = new CreateAddressCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "bad",
                "Calle Mayor 10",
                "Madrid",
                "ESP",
                null,
                null,
                LocalDate.of(2026, 1, 10),
                null
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));

        assertThrows(AddressCatalogValueInvalidException.class, () -> service.create(command));
        verify(addressRepository, never()).save(any(Address.class));
    }

    @Test
    void rejectsInvalidDateRangeWhenEndDateIsBeforeStartDate() {
        CreateAddressCommand command = new CreateAddressCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "HOME",
                "Calle Mayor 10",
                "Madrid",
                "ESP",
                null,
                null,
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 1, 9)
        );

        when(ruleSystemRepository.findByCode(RULE_SYSTEM_CODE)).thenReturn(Optional.of(ruleSystem(RULE_SYSTEM_CODE)));
        when(employeeAddressLookupPort.findByBusinessKeyForUpdate(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(addressRepository.findMaxAddressNumberByEmployeeId(10L)).thenReturn(Optional.empty());

        assertThrows(InvalidAddressDateRangeException.class, () -> service.create(command));
        verify(addressRepository, never()).save(any(Address.class));
    }

    private EmployeeAddressContext employeeContext(Long employeeId) {
        return new EmployeeAddressContext(employeeId, RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }

    private static final class TestAddressCatalogValidator extends AddressCatalogValidator {

        private TestAddressCatalogValidator() {
            super(null);
        }

        @Override
        public void validateAddressTypeCode(String ruleSystemCode, String addressTypeCode, LocalDate referenceDate) {
            if ("BAD".equals(addressTypeCode)) {
                throw new AddressCatalogValueInvalidException("addressTypeCode", addressTypeCode);
            }
        }

        @Override
        public String normalizeRequiredCode(String fieldName, String value) {
            if (value == null || value.trim().isEmpty()) {
                throw new AddressCatalogValueInvalidException(fieldName, String.valueOf(value));
            }

            return value.trim().toUpperCase();
        }
    }

    private RuleSystem ruleSystem(String code) {
        return new RuleSystem(
                1L,
                code,
                "Spain",
                "ESP",
                true,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
