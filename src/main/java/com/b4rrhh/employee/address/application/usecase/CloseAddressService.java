package com.b4rrhh.employee.address.application.usecase;

import com.b4rrhh.employee.address.application.port.EmployeeAddressContext;
import com.b4rrhh.employee.address.application.port.EmployeeAddressLookupPort;
import com.b4rrhh.employee.address.domain.exception.AddressEmployeeNotFoundException;
import com.b4rrhh.employee.address.domain.exception.AddressNotFoundException;
import com.b4rrhh.employee.address.domain.exception.AddressRuleSystemNotFoundException;
import com.b4rrhh.employee.address.domain.model.Address;
import com.b4rrhh.employee.address.domain.port.AddressRepository;
import com.b4rrhh.rulesystem.domain.port.RuleSystemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CloseAddressService implements CloseAddressUseCase {

    private final AddressRepository addressRepository;
    private final EmployeeAddressLookupPort employeeAddressLookupPort;
    private final RuleSystemRepository ruleSystemRepository;

    public CloseAddressService(
            AddressRepository addressRepository,
            EmployeeAddressLookupPort employeeAddressLookupPort,
            RuleSystemRepository ruleSystemRepository
    ) {
        this.addressRepository = addressRepository;
        this.employeeAddressLookupPort = employeeAddressLookupPort;
        this.ruleSystemRepository = ruleSystemRepository;
    }

    @Override
    @Transactional
    public Address close(CloseAddressCommand command) {
        String normalizedRuleSystemCode = normalizeRuleSystemCode(command.ruleSystemCode());
        String normalizedEmployeeTypeCode = normalizeEmployeeTypeCode(command.employeeTypeCode());
        String normalizedEmployeeNumber = normalizeEmployeeNumber(command.employeeNumber());
        Integer normalizedAddressNumber = normalizeAddressNumber(command.addressNumber());

        ruleSystemRepository.findByCode(normalizedRuleSystemCode)
                .orElseThrow(() -> new AddressRuleSystemNotFoundException(normalizedRuleSystemCode));

        EmployeeAddressContext employee = employeeAddressLookupPort
                .findByBusinessKeyForUpdate(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                )
                .orElseThrow(() -> new AddressEmployeeNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber
                ));

        Address existing = addressRepository
                .findByEmployeeIdAndAddressNumber(employee.employeeId(), normalizedAddressNumber)
                .orElseThrow(() -> new AddressNotFoundException(
                        normalizedRuleSystemCode,
                        normalizedEmployeeTypeCode,
                        normalizedEmployeeNumber,
                        normalizedAddressNumber
                ));

        Address closed = existing.close(command.endDate());
        return addressRepository.save(closed);
    }

    private String normalizeRuleSystemCode(String ruleSystemCode) {
        if (ruleSystemCode == null || ruleSystemCode.trim().isEmpty()) {
            throw new IllegalArgumentException("ruleSystemCode is required");
        }

        return ruleSystemCode.trim().toUpperCase();
    }

    private String normalizeEmployeeTypeCode(String employeeTypeCode) {
        if (employeeTypeCode == null || employeeTypeCode.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeTypeCode is required");
        }

        return employeeTypeCode.trim().toUpperCase();
    }

    private String normalizeEmployeeNumber(String employeeNumber) {
        if (employeeNumber == null || employeeNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("employeeNumber is required");
        }

        return employeeNumber.trim();
    }

    private Integer normalizeAddressNumber(Integer addressNumber) {
        if (addressNumber == null || addressNumber <= 0) {
            throw new IllegalArgumentException("addressNumber must be a positive integer");
        }

        return addressNumber;
    }
}
