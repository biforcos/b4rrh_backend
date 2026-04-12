package com.b4rrhh.payroll.application.usecase;

import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollConcept;
import com.b4rrhh.payroll.domain.model.PayrollContextSnapshot;
import com.b4rrhh.payroll.domain.model.PayrollStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CalculatePayrollUnitService implements CalculatePayrollUnitUseCase {

    private final CalculatePayrollUseCase calculatePayrollUseCase;

    public CalculatePayrollUnitService(CalculatePayrollUseCase calculatePayrollUseCase) {
        this.calculatePayrollUseCase = calculatePayrollUseCase;
    }

    @Override
    public Payroll calculate(CalculatePayrollUnitCommand command) {
        return calculatePayrollUseCase.calculate(new CalculatePayrollCommand(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber(),
                command.payrollPeriodCode(),
                command.payrollTypeCode(),
                command.presenceNumber(),
                PayrollStatus.CALCULATED,
                null,
                LocalDateTime.now(),
                command.calculationEngineCode(),
                command.calculationEngineVersion(),
                fakeConcepts(command),
                List.of(fakeSnapshot(command))
        ));
    }

            private List<PayrollConcept> fakeConcepts(CalculatePayrollUnitCommand command) {
            return List.of(
                new PayrollConcept(
                    1,
                    "BASE_FAKE",
                    "Base fake amount",
                    new BigDecimal("1000.00"),
                    BigDecimal.ONE,
                    new BigDecimal("1000.00"),
                    "EARNING",
                    command.payrollPeriodCode(),
                    1
                ),
                new PayrollConcept(
                    2,
                    "NET_FAKE",
                    "Net fake amount",
                    new BigDecimal("800.00"),
                    BigDecimal.ONE,
                    new BigDecimal("800.00"),
                    "NET",
                    command.payrollPeriodCode(),
                    2
                )
            );
            }

            private PayrollContextSnapshot fakeSnapshot(CalculatePayrollUnitCommand command) {
            return new PayrollContextSnapshot(
                "EMPLOYEE_PAYROLL_CONTEXT",
                "PAYROLL_LAUNCH",
                "{\"ruleSystemCode\":\"" + command.ruleSystemCode() + "\",\"employeeTypeCode\":\""
                    + command.employeeTypeCode() + "\",\"employeeNumber\":\"" + command.employeeNumber()
                    + "\",\"payrollPeriodCode\":\"" + command.payrollPeriodCode() + "\",\"payrollTypeCode\":\""
                    + command.payrollTypeCode() + "\",\"presenceNumber\":" + command.presenceNumber() + "}",
                "{\"calculationEngineCode\":\"" + command.calculationEngineCode() + "\",\"calculationEngineVersion\":\""
                    + command.calculationEngineVersion() + "\",\"mode\":\"DETERMINISTIC_FAKE\"}"
            );
            }
}