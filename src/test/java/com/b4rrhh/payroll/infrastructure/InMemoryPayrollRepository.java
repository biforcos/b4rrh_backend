package com.b4rrhh.payroll.infrastructure;

import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.port.PayrollRepository;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryPayrollRepository implements PayrollRepository {

    private final Map<Long, Payroll> storage = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Payroll save(Payroll payroll) {
        Long id = payroll.getId() == null ? sequence.incrementAndGet() : payroll.getId();
        Payroll persisted = Payroll.rehydrate(
                id,
                payroll.getRuleSystemCode(),
                payroll.getEmployeeTypeCode(),
                payroll.getEmployeeNumber(),
                payroll.getPayrollPeriodCode(),
                payroll.getPayrollTypeCode(),
                payroll.getPresenceNumber(),
                payroll.getStatus(),
                payroll.getStatusReasonCode(),
                payroll.getCalculatedAt(),
                payroll.getCalculationEngineCode(),
                payroll.getCalculationEngineVersion(),
                payroll.getConcepts(),
                payroll.getContextSnapshots(),
                payroll.getCreatedAt() == null ? LocalDateTime.now() : payroll.getCreatedAt(),
                LocalDateTime.now()
        );
        storage.put(id, persisted);
        return persisted;
    }

    @Override
    public void flush() {
        // No-op for in-memory implementation
    }

    @Override
    public void deleteById(Long id) {
        storage.remove(id);
    }

    @Override
    public Optional<Payroll> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String payrollPeriodCode,
            String payrollTypeCode,
            Integer presenceNumber
    ) {
        return storage.values().stream()
                .filter(payroll -> payroll.getRuleSystemCode().equals(ruleSystemCode)
                        && payroll.getEmployeeTypeCode().equals(employeeTypeCode)
                        && payroll.getEmployeeNumber().equals(employeeNumber)
                        && payroll.getPayrollPeriodCode().equals(payrollPeriodCode)
                        && payroll.getPayrollTypeCode().equals(payrollTypeCode)
                        && payroll.getPresenceNumber().equals(presenceNumber))
                .findFirst();
    }
}