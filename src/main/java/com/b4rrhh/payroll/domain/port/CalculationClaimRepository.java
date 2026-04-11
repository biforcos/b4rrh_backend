package com.b4rrhh.payroll.domain.port;

import com.b4rrhh.payroll.domain.model.CalculationClaim;

public interface CalculationClaimRepository {

    CalculationClaim save(CalculationClaim calculationClaim);

    void deleteById(Long id);
}