package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.port.PayrollRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchPayrollsService implements SearchPayrollsUseCase {

    private final PayrollRepository payrollRepository;

    public SearchPayrollsService(PayrollRepository payrollRepository) {
        this.payrollRepository = payrollRepository;
    }

    @Override
    public List<Payroll> search(SearchPayrollsQuery query) {
        return payrollRepository.findByFilters(
                query.ruleSystemCode(),
                query.payrollPeriodCode(),
                query.employeeNumber(),
                query.status()
        );
    }
}
