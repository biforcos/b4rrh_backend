package com.b4rrhh.employee.payroll_input.infrastructure.persistence;

import com.b4rrhh.employee.payroll_input.domain.model.EmployeePayrollInput;
import com.b4rrhh.employee.payroll_input.domain.port.EmployeePayrollInputRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class EmployeePayrollInputPersistenceAdapter implements EmployeePayrollInputRepository {

    private final SpringDataEmployeePayrollInputRepository springDataRepo;

    public EmployeePayrollInputPersistenceAdapter(
            SpringDataEmployeePayrollInputRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public boolean existsByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                        String employeeNumber, String conceptCode, int period) {
        return springDataRepo
                .existsByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
                        ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period);
    }

    @Override
    public Optional<EmployeePayrollInput> findByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                                             String employeeNumber, String conceptCode,
                                                             int period) {
        return springDataRepo
                .findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
                        ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period)
                .map(this::toDomain);
    }

    @Override
    public List<EmployeePayrollInput> findByEmployeeAndPeriod(String ruleSystemCode,
                                                               String employeeTypeCode,
                                                               String employeeNumber, int period) {
        return springDataRepo
                .findByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndPeriodOrderByConceptCode(
                        ruleSystemCode, employeeTypeCode, employeeNumber, period)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public EmployeePayrollInput save(EmployeePayrollInput input) {
        EmployeePayrollInputEntity saved = springDataRepo.save(toEntity(input));
        return toDomain(saved);
    }

    @Override
    public void deleteByBusinessKey(String ruleSystemCode, String employeeTypeCode,
                                     String employeeNumber, String conceptCode, int period) {
        springDataRepo
                .deleteByRuleSystemCodeAndEmployeeTypeCodeAndEmployeeNumberAndConceptCodeAndPeriod(
                        ruleSystemCode, employeeTypeCode, employeeNumber, conceptCode, period);
    }

    private EmployeePayrollInput toDomain(EmployeePayrollInputEntity entity) {
        return EmployeePayrollInput.rehydrate(
                entity.getRuleSystemCode(),
                entity.getEmployeeTypeCode(),
                entity.getEmployeeNumber(),
                entity.getConceptCode(),
                entity.getPeriod(),
                entity.getQuantity()
        );
    }

    private EmployeePayrollInputEntity toEntity(EmployeePayrollInput input) {
        EmployeePayrollInputEntity entity = new EmployeePayrollInputEntity();
        entity.setRuleSystemCode(input.getRuleSystemCode());
        entity.setEmployeeTypeCode(input.getEmployeeTypeCode());
        entity.setEmployeeNumber(input.getEmployeeNumber());
        entity.setConceptCode(input.getConceptCode());
        entity.setPeriod(input.getPeriod());
        entity.setQuantity(input.getQuantity());
        return entity;
    }
}
