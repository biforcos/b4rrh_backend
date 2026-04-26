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

    /**
     * When true (default), EARNING/DEDUCTION rows produced by multiple working-time
     * segments are collapsed into a single row per (conceptCode, rate) pair.
     * Set to false to see one row per segment in the database — useful for debugging.
     */
    private boolean collapseSegmentRows = true;

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

    public boolean isCollapseSegmentRows() {
        return collapseSegmentRows;
    }

    public void setCollapseSegmentRows(boolean collapseSegmentRows) {
        this.collapseSegmentRows = collapseSegmentRows;
    }
}
