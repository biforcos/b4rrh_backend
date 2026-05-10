package com.b4rrhh.employee.lifecycle.application.participant;

import com.b4rrhh.employee.labor_classification.application.command.CreateLaborClassificationCommand;
import com.b4rrhh.employee.labor_classification.application.usecase.CreateLaborClassificationUseCase;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementCategoryRelationInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationAgreementInvalidException;
import com.b4rrhh.employee.labor_classification.domain.exception.LaborClassificationCategoryInvalidException;
import com.b4rrhh.employee.lifecycle.application.model.HireContext;
import com.b4rrhh.employee.lifecycle.application.port.HireParticipant;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeCatalogValueInvalidException;
import com.b4rrhh.employee.lifecycle.domain.exception.HireEmployeeDependentRelationInvalidException;
import org.springframework.stereotype.Component;

@Component
public class LaborClassificationParticipant implements HireParticipant {

    private final CreateLaborClassificationUseCase createLaborClassificationUseCase;

    public LaborClassificationParticipant(CreateLaborClassificationUseCase createLaborClassificationUseCase) {
        this.createLaborClassificationUseCase = createLaborClassificationUseCase;
    }

    @Override
    public int order() {
        return 60;
    }

    @Override
    public void participate(HireContext ctx) {
        try {
            ctx.setLaborClassificationResult(createLaborClassificationUseCase.create(
                    new CreateLaborClassificationCommand(
                            ctx.ruleSystemCode(), ctx.employeeTypeCode(), ctx.employeeNumber(),
                            ctx.laborClassification().agreementCode(),
                            ctx.laborClassification().agreementCategoryCode(),
                            ctx.hireDate(), null
                    )));
        } catch (LaborClassificationAgreementInvalidException | LaborClassificationCategoryInvalidException ex) {
            throw new HireEmployeeCatalogValueInvalidException(ex.getMessage(), ex);
        } catch (LaborClassificationAgreementCategoryRelationInvalidException ex) {
            throw new HireEmployeeDependentRelationInvalidException(ex.getMessage(), ex);
        }
    }
}
