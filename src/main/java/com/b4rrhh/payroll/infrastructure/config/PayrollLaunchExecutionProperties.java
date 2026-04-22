package com.b4rrhh.payroll.infrastructure.config;

import com.b4rrhh.payroll.application.usecase.PayrollExecutionMode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Configuration for payroll launch unit execution path.
 */
@Component
@ConfigurationProperties(prefix = "payroll.launch.execution")
public class PayrollLaunchExecutionProperties {

    /**
     * Default execution mode remains FAKE for backward compatibility.
     */
    private PayrollExecutionMode mode = PayrollExecutionMode.FAKE;

    /**
     * Temporary configurable monthly salary amount used by ELIGIBLE_REAL mode.
     *
     * <p>If null, ELIGIBLE_REAL skips units with explicit reporting because the launcher
     * does not yet provide a reliable monthly salary source.
     */
    private BigDecimal eligibleRealMonthlySalaryAmount;

    public PayrollExecutionMode getMode() {
        return mode;
    }

    public void setMode(PayrollExecutionMode mode) {
        this.mode = mode;
    }

    public BigDecimal getEligibleRealMonthlySalaryAmount() {
        return eligibleRealMonthlySalaryAmount;
    }

    public void setEligibleRealMonthlySalaryAmount(BigDecimal eligibleRealMonthlySalaryAmount) {
        this.eligibleRealMonthlySalaryAmount = eligibleRealMonthlySalaryAmount;
    }
}
