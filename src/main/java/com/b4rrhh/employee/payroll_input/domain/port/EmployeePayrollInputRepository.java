package com.b4rrhh.employee.payroll_input.domain.port;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;

import java.util.List;
import java.util.Optional;

public interface EmployeePayrollInputRepository {

    boolean existsByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                 String employeeNumber, String conceptCode, int period);

    Optional<EmployeePayrollInput> findByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                                      String employeeNumber, String conceptCode, int period);

    List<EmployeePayrollInput> findByEmployeeAndPeriod(String ruleSystemCode, String employeeTypeCode,
                                                        String employeeNumber, int period);

    EmployeePayrollInput save(EmployeePayrollInput input);

    void deleteByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                              String employeeNumber, String conceptCode, int period);
}
