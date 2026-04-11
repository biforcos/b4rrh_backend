package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.domain.model.CalculationRunMessage;
import com.b4rrhh.payroll.domain.port.CalculationRunMessageRepository;
import org.springframework.stereotype.Component;

@Component
public class CalculationRunMessagePersistenceAdapter implements CalculationRunMessageRepository {

    private final SpringDataCalculationRunMessageRepository springDataCalculationRunMessageRepository;

    public CalculationRunMessagePersistenceAdapter(SpringDataCalculationRunMessageRepository springDataCalculationRunMessageRepository) {
        this.springDataCalculationRunMessageRepository = springDataCalculationRunMessageRepository;
    }

    @Override
    public CalculationRunMessage save(CalculationRunMessage calculationRunMessage) {
        CalculationRunMessageEntity entity = new CalculationRunMessageEntity();
        entity.setId(calculationRunMessage.id());
        entity.setCalculationRun(runReference(calculationRunMessage.runId()));
        entity.setMessageCode(calculationRunMessage.messageCode());
        entity.setSeverityCode(calculationRunMessage.severityCode());
        entity.setMessage(calculationRunMessage.message());
        entity.setDetailsJson(calculationRunMessage.detailsJson());
        entity.setRuleSystemCode(calculationRunMessage.ruleSystemCode());
        entity.setEmployeeTypeCode(calculationRunMessage.employeeTypeCode());
        entity.setEmployeeNumber(calculationRunMessage.employeeNumber());
        entity.setPayrollPeriodCode(calculationRunMessage.payrollPeriodCode());
        entity.setPayrollTypeCode(calculationRunMessage.payrollTypeCode());
        entity.setPresenceNumber(calculationRunMessage.presenceNumber());
        entity.setCreatedAt(calculationRunMessage.createdAt());

        CalculationRunMessageEntity saved = springDataCalculationRunMessageRepository.save(entity);
        return new CalculationRunMessage(
                saved.getId(),
                saved.getCalculationRun().getId(),
                saved.getMessageCode(),
                saved.getSeverityCode(),
                saved.getMessage(),
                saved.getDetailsJson(),
                saved.getRuleSystemCode(),
                saved.getEmployeeTypeCode(),
                saved.getEmployeeNumber(),
                saved.getPayrollPeriodCode(),
                saved.getPayrollTypeCode(),
                saved.getPresenceNumber(),
                saved.getCreatedAt()
        );
    }

    private CalculationRunEntity runReference(Long runId) {
        CalculationRunEntity run = new CalculationRunEntity();
        run.setId(runId);
        return run;
    }
}