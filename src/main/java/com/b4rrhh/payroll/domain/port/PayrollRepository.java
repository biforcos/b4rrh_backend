package com.b4rrhh.payroll.domain.port;

import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollStatus;

import java.util.List;
import java.util.Optional;

public interface PayrollRepository {

    Optional<Payroll> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String payrollPeriodCode,
            String payrollTypeCode,
            Integer presenceNumber
    );

    List<Payroll> findByFilters(String ruleSystemCode, String payrollPeriodCode, String employeeNumber, PayrollStatus status);

    Payroll save(Payroll payroll);

    void deleteById(Long id);

    void flush();

}