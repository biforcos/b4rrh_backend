package com.b4rrhh.payroll.application.service;

public interface PayrollConceptGraphCalculator {

    PayrollConceptExecutionResult calculateConceptResult(String conceptCode, PayrollConceptExecutionContext context);
}