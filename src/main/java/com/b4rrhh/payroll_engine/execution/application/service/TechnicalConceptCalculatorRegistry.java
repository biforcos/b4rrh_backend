package com.b4rrhh.payroll_engine.execution.application.service;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class TechnicalConceptCalculatorRegistry {

    private final Map<String, TechnicalConceptCalculator> calculators;

    public TechnicalConceptCalculatorRegistry(List<TechnicalConceptCalculator> calculators) {
        this.calculators = calculators.stream()
                .collect(Collectors.toMap(TechnicalConceptCalculator::conceptCode, c -> c));
    }

    public TechnicalConceptCalculator get(String conceptCode) {
        return calculators.get(conceptCode);
    }
}
