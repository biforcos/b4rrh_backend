package com.b4rrhh.employee.workcenter.infrastructure.web.assembler;

import com.b4rrhh.employee.workcenter.application.port.WorkCenterCatalogReadPort;
import com.b4rrhh.employee.workcenter.domain.model.WorkCenter;
import com.b4rrhh.employee.workcenter.infrastructure.web.dto.WorkCenterResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("employeeWorkCenterResponseAssembler")
public class WorkCenterResponseAssembler {

    private final WorkCenterCatalogReadPort workCenterCatalogReadPort;

    public WorkCenterResponseAssembler(WorkCenterCatalogReadPort workCenterCatalogReadPort) {
        this.workCenterCatalogReadPort = workCenterCatalogReadPort;
    }

    public WorkCenterResponse toResponse(String ruleSystemCode, WorkCenter workCenter) {
        String workCenterName = workCenterCatalogReadPort
                .findWorkCenterName(ruleSystemCode, workCenter.getWorkCenterCode())
                .orElse(null);
        String companyCode = workCenterCatalogReadPort
            .findWorkCenterCompanyCode(ruleSystemCode, workCenter.getWorkCenterCode(), workCenter.getStartDate())
            .orElse(null);
        String companyName = companyCode == null
            ? null
            : workCenterCatalogReadPort.findCompanyName(ruleSystemCode, companyCode).orElse(null);

        return new WorkCenterResponse(
                workCenter.getWorkCenterAssignmentNumber(),
                workCenter.getWorkCenterCode(),
                workCenterName,
            companyCode,
            companyName,
                workCenter.getStartDate(),
                workCenter.getEndDate()
        );
    }

    public List<WorkCenterResponse> toResponseList(String ruleSystemCode, List<WorkCenter> workCenters) {
        return workCenters.stream()
                .map(workCenter -> toResponse(ruleSystemCode, workCenter))
                .toList();
    }
}