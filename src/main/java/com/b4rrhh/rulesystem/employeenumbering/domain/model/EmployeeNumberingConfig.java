package com.b4rrhh.rulesystem.employeenumbering.domain.model;

public record EmployeeNumberingConfig(
        String ruleSystemCode,
        String prefix,
        int numericPartLength,
        int step,
        long nextValue
) {
    public String formatNumber() {
        return prefix + String.format("%0" + numericPartLength + "d", nextValue);
    }

    public boolean isExhausted() {
        long max = (long) Math.pow(10, numericPartLength) - 1;
        return nextValue > max;
    }

    public EmployeeNumberingConfig advance() {
        return new EmployeeNumberingConfig(ruleSystemCode, prefix, numericPartLength, step, nextValue + step);
    }
}
