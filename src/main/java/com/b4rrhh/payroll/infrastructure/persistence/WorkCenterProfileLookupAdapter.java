package com.b4rrhh.payroll.infrastructure.persistence;

import com.b4rrhh.employee.workcenter.application.port.WorkCenterCatalogReadPort;
import com.b4rrhh.payroll.application.port.WorkCenterProfileContext;
import com.b4rrhh.payroll.application.port.WorkCenterProfileLookupPort;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class WorkCenterProfileLookupAdapter implements WorkCenterProfileLookupPort {

    private final WorkCenterCatalogReadPort workCenterCatalogReadPort;

    public WorkCenterProfileLookupAdapter(WorkCenterCatalogReadPort workCenterCatalogReadPort) {
        this.workCenterCatalogReadPort = workCenterCatalogReadPort;
    }

    @Override
    public Optional<WorkCenterProfileContext> findByRuleSystemAndCode(String ruleSystemCode, String workCenterCode) {
        if (workCenterCode == null || workCenterCode.isBlank()) return Optional.empty();
        String name = workCenterCatalogReadPort.findWorkCenterName(ruleSystemCode, workCenterCode).orElse(null);
        return Optional.of(new WorkCenterProfileContext(workCenterCode, name));
    }
}
