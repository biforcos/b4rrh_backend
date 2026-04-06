package com.b4rrhh.rulesystem.workcenter.infrastructure.web.mapper;

import com.b4rrhh.rulesystem.workcenter.application.usecase.CreateWorkCenterContactCommand;
import com.b4rrhh.rulesystem.workcenter.application.usecase.DeleteWorkCenterContactCommand;
import com.b4rrhh.rulesystem.workcenter.application.usecase.GetWorkCenterContactQuery;
import com.b4rrhh.rulesystem.workcenter.application.usecase.UpdateWorkCenterContactCommand;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.CreateWorkCenterContactRequest;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.UpdateWorkCenterContactRequest;
import org.springframework.stereotype.Component;

@Component
public class WorkCenterContactCommandMapper {

    public CreateWorkCenterContactCommand toCreateCommand(
            String ruleSystemCode,
            String workCenterCode,
            CreateWorkCenterContactRequest request
    ) {
        return new CreateWorkCenterContactCommand(
                ruleSystemCode,
                workCenterCode,
                request.contactTypeCode(),
                request.contactValue()
        );
    }

    public UpdateWorkCenterContactCommand toUpdateCommand(
            String ruleSystemCode,
            String workCenterCode,
            Integer contactNumber,
            UpdateWorkCenterContactRequest request
    ) {
        return new UpdateWorkCenterContactCommand(
                ruleSystemCode,
                workCenterCode,
                contactNumber,
                request.contactTypeCode(),
                request.contactValue()
        );
    }

    public GetWorkCenterContactQuery toGetQuery(String ruleSystemCode, String workCenterCode, Integer contactNumber) {
        return new GetWorkCenterContactQuery(ruleSystemCode, workCenterCode, contactNumber);
    }

    public DeleteWorkCenterContactCommand toDeleteCommand(String ruleSystemCode, String workCenterCode, Integer contactNumber) {
        return new DeleteWorkCenterContactCommand(ruleSystemCode, workCenterCode, contactNumber);
    }
}