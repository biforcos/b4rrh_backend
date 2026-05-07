package com.b4rrhh.employee.tax_information.infrastructure.web.assembler;

import com.b4rrhh.employee.tax_information.application.usecase.*;
import com.b4rrhh.employee.tax_information.domain.model.*;
import com.b4rrhh.employee.tax_information.infrastructure.web.dto.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;

@Component
public class EmployeeTaxInformationAssembler {

    public EmployeeTaxInformationResponse toResponse(EmployeeTaxInformation domain) {
        return new EmployeeTaxInformationResponse(
            domain.getValidFrom().toString(),
            domain.getFamilySituation().name(),
            domain.getDescendantsCount(),
            domain.getAscendantsCount(),
            domain.getDisabilityDegree().name(),
            domain.isPensionCompensatoria(),
            domain.isGeographicMobility(),
            domain.isHabitualResidenceLoan(),
            domain.getTaxTerritory().name()
        );
    }

    public List<EmployeeTaxInformationResponse> toResponseList(List<EmployeeTaxInformation> list) {
        return list.stream().map(this::toResponse).toList();
    }

    public CreateEmployeeTaxInformationCommand toCreateCommand(
            String ruleSystemCode, String employeeTypeCode, String employeeNumber,
            CreateEmployeeTaxInformationRequest req) {
        return new CreateEmployeeTaxInformationCommand(
            ruleSystemCode, employeeTypeCode, employeeNumber,
            req.getValidFrom(),
            FamilySituation.valueOf(req.getFamilySituation()),
            req.getDescendantsCount(), req.getAscendantsCount(),
            DisabilityDegree.valueOf(req.getDisabilityDegree()),
            req.isPensionCompensatoria(), req.isGeographicMobility(),
            req.isHabitualResidenceLoan(),
            TaxTerritory.valueOf(req.getTaxTerritory())
        );
    }

    public CorrectEmployeeTaxInformationCommand toCorrectCommand(
            String ruleSystemCode, String employeeTypeCode, String employeeNumber,
            LocalDate validFrom,
            CorrectEmployeeTaxInformationRequest req) {
        return new CorrectEmployeeTaxInformationCommand(
            ruleSystemCode, employeeTypeCode, employeeNumber,
            validFrom,
            FamilySituation.valueOf(req.getFamilySituation()),
            req.getDescendantsCount(), req.getAscendantsCount(),
            DisabilityDegree.valueOf(req.getDisabilityDegree()),
            req.isPensionCompensatoria(), req.isGeographicMobility(),
            req.isHabitualResidenceLoan(),
            TaxTerritory.valueOf(req.getTaxTerritory())
        );
    }
}
