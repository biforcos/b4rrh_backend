package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;

import java.util.List;

public interface SearchPayrollsUseCase {
    List<Payroll> search(SearchPayrollsQuery query);
}
