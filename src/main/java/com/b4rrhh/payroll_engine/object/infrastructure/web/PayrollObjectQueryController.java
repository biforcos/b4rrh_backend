package com.b4rrhh.payroll_engine.object.infrastructure.web;

import com.b4rrhh.payroll_engine.object.domain.model.PayrollObjectTypeCode;
import com.b4rrhh.payroll_engine.object.domain.port.PayrollObjectRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/payroll-engine/{ruleSystemCode}/objects")
public class PayrollObjectQueryController {

    private final PayrollObjectRepository repository;

    public PayrollObjectQueryController(PayrollObjectRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<PayrollObjectResponse>> list(
            @PathVariable String ruleSystemCode,
            @RequestParam String type
    ) {
        PayrollObjectTypeCode typeCode = PayrollObjectTypeCode.valueOf(type);
        List<PayrollObjectResponse> response = repository
                .findAllByType(ruleSystemCode, typeCode)
                .stream()
                .map(PayrollObjectResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
