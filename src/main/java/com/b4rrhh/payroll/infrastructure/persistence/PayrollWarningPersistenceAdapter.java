package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.payroll.domain.model.PayrollWarning;
import com.b4rrhh.payroll.domain.port.PayrollWarningRepository;
import org.springframework.stereotype.Component;

@Component
public class PayrollWarningPersistenceAdapter implements PayrollWarningRepository {

    private final SpringDataPayrollWarningRepository springDataPayrollWarningRepository;

    public PayrollWarningPersistenceAdapter(SpringDataPayrollWarningRepository springDataPayrollWarningRepository) {
        this.springDataPayrollWarningRepository = springDataPayrollWarningRepository;
    }

    @Override
    public PayrollWarning save(PayrollWarning payrollWarning) {
        PayrollWarningEntity entity = new PayrollWarningEntity();
        entity.setId(payrollWarning.id());
        entity.setPayroll(payrollReference(payrollWarning.payrollId()));
        entity.setWarningCode(payrollWarning.warningCode());
        entity.setSeverityCode(payrollWarning.severityCode());
        entity.setMessage(payrollWarning.message());
        entity.setDetailsJson(payrollWarning.detailsJson());

        PayrollWarningEntity saved = springDataPayrollWarningRepository.save(entity);
        return new PayrollWarning(
                saved.getId(),
                saved.getPayroll().getId(),
                saved.getWarningCode(),
                saved.getSeverityCode(),
                saved.getMessage(),
                saved.getDetailsJson()
        );
    }

    private PayrollEntity payrollReference(Long payrollId) {
        PayrollEntity payroll = new PayrollEntity();
        payroll.setId(payrollId);
        return payroll;
    }
}