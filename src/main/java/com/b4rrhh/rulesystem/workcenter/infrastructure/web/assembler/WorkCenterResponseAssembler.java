package com.b4rrhh.rulesystem.workcenter.infrastructure.web.assembler;

import com.b4rrhh.rulesystem.workcenter.application.view.WorkCenterDetails;
import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenter;
import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterContact;
import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterProfile;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.WorkCenterAddress;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.WorkCenterContactResponse;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.WorkCenterListItemResponse;
import com.b4rrhh.rulesystem.workcenter.infrastructure.web.dto.WorkCenterResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkCenterResponseAssembler {

    public WorkCenterResponse toResponse(WorkCenterDetails details) {
        WorkCenter workCenter = details.workCenter();
        WorkCenterProfile profile = details.profile();

        return new WorkCenterResponse(
                workCenter.ruleSystemCode(),
                workCenter.workCenterCode(),
                workCenter.name(),
                workCenter.description(),
                workCenter.startDate(),
                workCenter.endDate(),
                workCenter.active(),
                profile.getCompanyCode(),
                toAddress(profile)
        );
    }

    public WorkCenterListItemResponse toListItemResponse(WorkCenterDetails details) {
        WorkCenter workCenter = details.workCenter();
        WorkCenterProfile profile = details.profile();

        return new WorkCenterListItemResponse(
                workCenter.ruleSystemCode(),
                workCenter.workCenterCode(),
                workCenter.name(),
                profile.getCompanyCode(),
                profile.getAddress() == null ? null : profile.getAddress().getCity(),
                profile.getAddress() == null ? null : profile.getAddress().getCountryCode(),
                workCenter.active(),
                workCenter.startDate(),
                workCenter.endDate()
        );
    }

    public WorkCenterContactResponse toContactResponse(WorkCenterContact contact) {
        return new WorkCenterContactResponse(
                contact.getContactNumber(),
                contact.getContactTypeCode(),
                contact.getContactTypeName(),
                contact.getContactValue()
        );
    }

    private WorkCenterAddress toAddress(WorkCenterProfile profile) {
        if (profile.getAddress() == null) {
            return new WorkCenterAddress(null, null, null, null, null);
        }

        return new WorkCenterAddress(
                profile.getAddress().getStreet(),
                profile.getAddress().getCity(),
                profile.getAddress().getPostalCode(),
                profile.getAddress().getRegionCode(),
                profile.getAddress().getCountryCode()
        );
    }
}