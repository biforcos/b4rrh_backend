package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.port.PayrollRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GetPayrollByBusinessKeyService implements GetPayrollByBusinessKeyUseCase {

    private final PayrollRepository payrollRepository;

    public GetPayrollByBusinessKeyService(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @Override
    public Optional<Payroll> getByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String payrollPeriodCode,
            String payrollTypeCode,
            Integer presenceNumber
    ) {
        return payrollRepository.findByBusinessKey(
                normalizeCode(ruleSystemCode, "ruleSystemCode", 5),
                normalizeCode(employeeTypeCode, "employeeTypeCode", 30),
                normalizeText(employeeNumber, "employeeNumber", 15),
                normalizeCode(payrollPeriodCode, "payrollPeriodCode", 30),
                normalizeCode(payrollTypeCode, "payrollTypeCode", 30),
                normalizePositive(presenceNumber, "presenceNumber")
        );
    }

    private String normalizeCode(String value, String fieldName, int maxLength) {
        return normalizeText(value, fieldName, maxLength).toUpperCase();
    }

    private String normalizeText(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPayrollArgumentException(fieldName + " is required");
        }
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new InvalidPayrollArgumentException(fieldName + " exceeds max length " + maxLength);
        }
        return normalized;
    }

    private Integer normalizePositive(Integer value, String fieldName) {
        if (value == null || value <= 0) {
            throw new InvalidPayrollArgumentException(fieldName + " must be a positive integer");
        }
        return value;
    }
}