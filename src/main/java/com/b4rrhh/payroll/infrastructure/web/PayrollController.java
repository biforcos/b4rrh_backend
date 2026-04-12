package com.b4rrhh.payroll.infrastructure.web;

import com.b4rrhh.payroll.application.usecase.CalculatePayrollCommand;
import com.b4rrhh.payroll.application.usecase.CalculatePayrollUseCase;
import com.b4rrhh.payroll.application.usecase.FinalizePayrollCommand;
import com.b4rrhh.payroll.application.usecase.FinalizePayrollUseCase;
import com.b4rrhh.payroll.application.usecase.GetPayrollByBusinessKeyUseCase;
import com.b4rrhh.payroll.application.usecase.InvalidatePayrollCommand;
import com.b4rrhh.payroll.application.usecase.InvalidatePayrollUseCase;
import com.b4rrhh.payroll.application.usecase.ValidatePayrollCommand;
import com.b4rrhh.payroll.application.usecase.ValidatePayrollUseCase;
import com.b4rrhh.payroll.domain.model.Payroll;
import com.b4rrhh.payroll.domain.model.PayrollConcept;
import com.b4rrhh.payroll.domain.model.PayrollContextSnapshot;
import com.b4rrhh.payroll.infrastructure.web.assembler.PayrollResponseAssembler;
import com.b4rrhh.payroll.infrastructure.web.dto.CalculatePayrollRequest;
import com.b4rrhh.payroll.infrastructure.web.dto.InvalidatePayrollRequest;
import com.b4rrhh.payroll.infrastructure.web.dto.PayrollResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payrolls")
public class PayrollController {

    private final CalculatePayrollUseCase calculatePayrollUseCase;
    private final GetPayrollByBusinessKeyUseCase getPayrollByBusinessKeyUseCase;
    private final InvalidatePayrollUseCase invalidatePayrollUseCase;
    private final ValidatePayrollUseCase validatePayrollUseCase;
    private final FinalizePayrollUseCase finalizePayrollUseCase;
    private final PayrollResponseAssembler payrollResponseAssembler;

    public PayrollController(
            CalculatePayrollUseCase calculatePayrollUseCase,
            GetPayrollByBusinessKeyUseCase getPayrollByBusinessKeyUseCase,
            InvalidatePayrollUseCase invalidatePayrollUseCase,
            ValidatePayrollUseCase validatePayrollUseCase,
            FinalizePayrollUseCase finalizePayrollUseCase,
            PayrollResponseAssembler payrollResponseAssembler
    ) {
        this.calculatePayrollUseCase = calculatePayrollUseCase;
        this.getPayrollByBusinessKeyUseCase = getPayrollByBusinessKeyUseCase;
        this.invalidatePayrollUseCase = invalidatePayrollUseCase;
        this.validatePayrollUseCase = validatePayrollUseCase;
        this.finalizePayrollUseCase = finalizePayrollUseCase;
        this.payrollResponseAssembler = payrollResponseAssembler;
    }

    @GetMapping("/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{payrollPeriodCode}/{payrollTypeCode}/{presenceNumber}")
    public ResponseEntity<PayrollResponse> getByBusinessKey(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String payrollPeriodCode,
            @PathVariable String payrollTypeCode,
            @PathVariable Integer presenceNumber
    ) {
        return getPayrollByBusinessKeyUseCase.getByBusinessKey(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                payrollPeriodCode,
                payrollTypeCode,
                presenceNumber
        )
                .map(payrollResponseAssembler::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

        // Temporary stub endpoint used to materialize payroll results before launch and real engine flows exist.
        @PostMapping("/calculate")
        public ResponseEntity<PayrollResponse> calculateTemporaryStub(@RequestBody CalculatePayrollRequest request) {
        Payroll payroll = calculatePayrollUseCase.calculate(new CalculatePayrollCommand(
                request.ruleSystemCode(),
                request.employeeTypeCode(),
                request.employeeNumber(),
                request.payrollPeriodCode(),
                request.payrollTypeCode(),
                request.presenceNumber(),
                request.status(),
                request.statusReasonCode(),
                request.calculatedAt(),
                request.calculationEngineCode(),
                request.calculationEngineVersion(),
                java.util.List.of(),
                request.concepts().stream()
                        .map(concept -> new PayrollConcept(
                                concept.lineNumber(),
                                concept.conceptCode(),
                                concept.conceptLabel(),
                                concept.amount(),
                                concept.quantity(),
                                concept.rate(),
                                concept.conceptNatureCode(),
                                concept.originPeriodCode(),
                                concept.displayOrder()
                        ))
                        .toList(),
                request.contextSnapshots().stream()
                        .map(snapshot -> new PayrollContextSnapshot(
                                snapshot.snapshotTypeCode(),
                                snapshot.sourceVerticalCode(),
                                snapshot.sourceBusinessKeyJson(),
                                snapshot.snapshotPayloadJson()
                        ))
                        .toList()
        ));

        return ResponseEntity.status(HttpStatus.CREATED).body(payrollResponseAssembler.toResponse(payroll));
    }

    @PostMapping("/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{payrollPeriodCode}/{payrollTypeCode}/{presenceNumber}/invalidate")
    public ResponseEntity<PayrollResponse> invalidate(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String payrollPeriodCode,
            @PathVariable String payrollTypeCode,
            @PathVariable Integer presenceNumber,
            @RequestBody InvalidatePayrollRequest request
    ) {
        Payroll payroll = invalidatePayrollUseCase.invalidate(new InvalidatePayrollCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                payrollPeriodCode,
                payrollTypeCode,
                presenceNumber,
                request.statusReasonCode()
        ));

        return ResponseEntity.ok(payrollResponseAssembler.toResponse(payroll));
    }

    @PostMapping("/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{payrollPeriodCode}/{payrollTypeCode}/{presenceNumber}/validate")
    public ResponseEntity<PayrollResponse> validate(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String payrollPeriodCode,
            @PathVariable String payrollTypeCode,
            @PathVariable Integer presenceNumber
    ) {
        Payroll payroll = validatePayrollUseCase.validate(new ValidatePayrollCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                payrollPeriodCode,
                payrollTypeCode,
                presenceNumber
        ));

        return ResponseEntity.ok(payrollResponseAssembler.toResponse(payroll));
    }

    @PostMapping("/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{payrollPeriodCode}/{payrollTypeCode}/{presenceNumber}/finalize")
    public ResponseEntity<PayrollResponse> finalizePayroll(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @PathVariable String payrollPeriodCode,
            @PathVariable String payrollTypeCode,
            @PathVariable Integer presenceNumber
    ) {
        Payroll payroll = finalizePayrollUseCase.finalizePayroll(new FinalizePayrollCommand(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                payrollPeriodCode,
                payrollTypeCode,
                presenceNumber
        ));

        return ResponseEntity.ok(payrollResponseAssembler.toResponse(payroll));
    }
}