package com.b4rrhh.payroll.infrastructure.web.assembler;

import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollConceptResponse;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollContextSnapshotResponse;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollResponse;
import org.springframework.stereotype.Component;

@Component
public class PayrollResponseAssembler {

    public PayrollResponse toResponse(Payroll payroll) {
        return new PayrollResponse(
                payroll.getRuleSystemCode(),
                payroll.getEmployeeTypeCode(),
                payroll.getEmployeeNumber(),
                payroll.getPayrollPeriodCode(),
                payroll.getPayrollTypeCode(),
                payroll.getPresenceNumber(),
                payroll.getStatus(),
                payroll.getStatusReasonCode(),
                payroll.getCalculatedAt(),
                payroll.getCalculationEngineCode(),
                payroll.getCalculationEngineVersion(),
                payroll.getConcepts().stream()
                        .map(concept -> new PayrollConceptResponse(
                                concept.getLineNumber(),
                                concept.getConceptCode(),
                                concept.getConceptLabel(),
                                concept.getAmount(),
                                concept.getQuantity(),
                                concept.getRate(),
                                concept.getConceptNatureCode(),
                                concept.getOriginPeriodCode(),
                                concept.getDisplayOrder()
                        ))
                        .toList(),
                payroll.getContextSnapshots().stream()
                        .map(snapshot -> new PayrollContextSnapshotResponse(
                                snapshot.getSnapshotTypeCode(),
                                snapshot.getSourceVerticalCode(),
                                snapshot.getSourceBusinessKeyJson(),
                                snapshot.getSnapshotPayloadJson()
                        ))
                        .toList()
        );
    }
}