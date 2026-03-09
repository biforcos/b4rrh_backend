package com.b4rrhh.employee.infrastructure.persistence;

import com.b4rrhh.employee.domain.model.Employee;
import com.b4rrhh.employee.domain.port.EmployeeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeePersistenceAdapter implements EmployeeRepository {

    private final SpringDataEmployeeRepository springDataEmployeeRepository;

    public EmployeePersistenceAdapter(SpringDataEmployeeRepository springDataEmployeeRepository) {
        this.springDataEmployeeRepository = springDataEmployeeRepository;
    }

    @Override
    public Optional<Employee> findByRuleSystemCodeAndEmployeeNumber(String ruleSystemCode, String employeeNumber) {
        return springDataEmployeeRepository.findByRuleSystemCodeAndEmployeeNumber(ruleSystemCode, employeeNumber)
                .map(this::mapToDomain);
    }

    @Override
    public Employee save(Employee employee) {
        EmployeeEntity entity = mapToEntity(employee);
        EmployeeEntity savedEntity = springDataEmployeeRepository.save(entity);
        return mapToDomain(savedEntity);
    }

private Employee mapToDomain(EmployeeEntity entity) {
    return new Employee(
            entity.getId(),
            entity.getRuleSystemCode(),
            entity.getEmployeeNumber(),
            entity.getFirstName(),
            entity.getLastName1(),
            entity.getLastName2(),
            entity.getPreferredName(),
            entity.getStatus(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
    );
}

    private EmployeeEntity mapToEntity(Employee employee) {
        EmployeeEntity entity = new EmployeeEntity();
        entity.setId(employee.getId());
        entity.setRuleSystemCode(employee.getRuleSystemCode());
        entity.setEmployeeNumber(employee.getEmployeeNumber());
        entity.setFirstName(employee.getFirstName());
        entity.setLastName1(employee.getLastName1());
        entity.setLastName2(employee.getLastName2());
        entity.setPreferredName(employee.getPreferredName());
        entity.setStatus(employee.getStatus());
        return entity;
    }
}