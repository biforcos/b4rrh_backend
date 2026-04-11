package com.b4rrhh.payroll.domain.port;

import com.b4rrhh.payroll.domain.model.PayrollWarning;

public interface PayrollWarningRepository {

    PayrollWarning save(PayrollWarning payrollWarning);
}