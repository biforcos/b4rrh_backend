package com.b4rrhh.payroll.infrastructure.web.assembler;

import com.b4rrhh.payroll.domain.model.CalculationRunMessage;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollCalculationRunMessageResponse;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollCalculationRunMessagesResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PayrollCalculationRunMessageResponseAssembler {

    public PayrollCalculationRunMessagesResponse toResponse(Long runId, List<CalculationRunMessage> messages) {
        return new PayrollCalculationRunMessagesResponse(
                runId,
                messages.stream().map(this::toItem).toList()
        );
    }

    private PayrollCalculationRunMessageResponse toItem(CalculationRunMessage message) {
        return new PayrollCalculationRunMessageResponse(
                message.messageCode(),
                message.severityCode(),
                message.message(),
                message.detailsJson(),
                message.ruleSystemCode(),
                message.employeeTypeCode(),
                message.employeeNumber(),
                message.payrollPeriodCode(),
                message.payrollTypeCode(),
                message.presenceNumber(),
                message.createdAt()
        );
    }
}