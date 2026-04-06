package com.b4rrhh.rulesystem.workcenter.infrastructure.web.mapper;

import com.b4rrhh.rulesystem.workcenter.application.usecase.CreateWorkCenterCommand;
import com.b4rrhh.rulesystem.workcenter.application.usecase.UpdateWorkCenterCommand;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.CreateWorkCenterRequest;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.UpdateWorkCenterRequest;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.WorkCenterAddress;
import org.springframework.stereotype.Component;

@Component
public class WorkCenterCommandMapper {

    public CreateWorkCenterCommand toCreateCommand(CreateWorkCenterRequest request) {
        WorkCenterAddress address = request.address();

        return new CreateWorkCenterCommand(
                request.ruleSystemCode(),
                request.workCenterCode(),
                request.name(),
                request.description(),
                request.startDate(),
                request.companyCode(),
                address == null ? null : address.street(),
                address == null ? null : address.city(),
                address == null ? null : address.postalCode(),
                address == null ? null : address.regionCode(),
                address == null ? null : address.countryCode()
        );
    }

    public UpdateWorkCenterCommand toUpdateCommand(
            String ruleSystemCode,
            String workCenterCode,
            UpdateWorkCenterRequest request
    ) {
        WorkCenterAddress address = request.address();

        return new UpdateWorkCenterCommand(
                ruleSystemCode,
                workCenterCode,
                request.name(),
                request.description(),
                request.companyCode(),
                address == null ? null : address.street(),
                address == null ? null : address.city(),
                address == null ? null : address.postalCode(),
                address == null ? null : address.regionCode(),
                address == null ? null : address.countryCode()
        );
    }
}