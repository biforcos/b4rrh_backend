package com.b4rrhh.payroll_engine.table.infrastructure.persistence;

import com.b4rrhh.payroll.basesalary.infrastructure.persistence.entity.PayrollTableRowEntity;
import com.b4rrhh.payroll.basesalary.infrastructure.persistence.repository.PayrollTableRowRepository;
import com.b4rrhh.payroll_engine.table.domain.model.PayrollTableRow;
import com.b4rrhh.payroll_engine.table.domain.port.PayrollTableRowManagementPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Component
public class PayrollTableRowManagementAdapter implements PayrollTableRowManagementPort {

    private final PayrollTableRowRepository repository;

    public PayrollTableRowManagementAdapter(PayrollTableRowRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PayrollTableRow> findAllByTableCode(String ruleSystemCode, String tableCode) {
        return repository.findByRuleSystemCodeAndTableCodeOrderBySearchCodeAscStartDateAsc(ruleSystemCode, tableCode)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public PayrollTableRow save(PayrollTableRow row) {
        PayrollTableRowEntity entity = toEntity(row);
        return toDomain(repository.save(entity));
    }

    @Override
    public Optional<PayrollTableRow> findById(Long id) {
        return repository.findById(id).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByBusinessKey(String ruleSystemCode, String tableCode, String searchCode, LocalDate startDate) {
        return repository.existsByRuleSystemCodeAndTableCodeAndSearchCodeAndStartDate(
                ruleSystemCode, tableCode, searchCode, startDate);
    }

    private PayrollTableRowEntity toEntity(PayrollTableRow domain) {
        PayrollTableRowEntity entity = new PayrollTableRowEntity();
        if (domain.getId() != null) entity.setId(domain.getId());
        entity.setRuleSystemCode(domain.getRuleSystemCode());
        entity.setTableCode(domain.getTableCode());
        entity.setSearchCode(domain.getSearchCode());
        entity.setStartDate(domain.getStartDate());
        entity.setEndDate(domain.getEndDate());
        entity.setMonthlyValue(domain.getMonthlyValue());
        entity.setAnnualValue(domain.getAnnualValue());
        entity.setDailyValue(domain.getDailyValue());
        entity.setHourlyValue(domain.getHourlyValue());
        entity.setActive(domain.isActive());
        return entity;
    }

    private PayrollTableRow toDomain(PayrollTableRowEntity entity) {
        return new PayrollTableRow(
                entity.getId(),
                entity.getRuleSystemCode(),
                entity.getTableCode(),
                entity.getSearchCode(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getMonthlyValue(),
                entity.getAnnualValue(),
                entity.getDailyValue(),
                entity.getHourlyValue(),
                Boolean.TRUE.equals(entity.getActive())
        );
    }
}
