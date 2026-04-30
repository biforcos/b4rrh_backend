package com.b4rrhh.payroll_engine.table.domain.port;

import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PayrollTableRowManagementPort {
    List<PayrollTableRow> findAllByTableCode(String ruleSystemCode, String tableCode);
    PayrollTableRow save(PayrollTableRow row);
    Optional<PayrollTableRow> findById(Long id);
    void deleteById(Long id);
    boolean existsByBusinessKey(String ruleSystemCode, String tableCode, String searchCode, LocalDate startDate);
}
