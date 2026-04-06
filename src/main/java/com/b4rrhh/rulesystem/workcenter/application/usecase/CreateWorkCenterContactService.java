package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.workcenter.application.port.WorkCenterContactCatalogReadPort;
import com.b4rrhh.rulesystem.workcenter.application.service.WorkCenterCatalogValidator;
import com.b4rrhh.rulesystem.workcenter.application.service.WorkCenterInputNormalizer;
import com.b4rrhh.rulesystem.workcenter.application.service.WorkCenterResolver;
import com.b4rrhh.rulesystem.workcenter.domain.model.WorkCenterContact;
import com.b4rrhh.rulesystem.workcenter.domain.port.WorkCenterContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class CreateWorkCenterContactService implements CreateWorkCenterContactUseCase {

    private final WorkCenterResolver workCenterResolver;
    private final WorkCenterInputNormalizer inputNormalizer;
    private final WorkCenterCatalogValidator catalogValidator;
    private final WorkCenterContactRepository workCenterContactRepository;
    private final WorkCenterContactCatalogReadPort workCenterContactCatalogReadPort;

    public CreateWorkCenterContactService(
            WorkCenterResolver workCenterResolver,
            WorkCenterInputNormalizer inputNormalizer,
            WorkCenterCatalogValidator catalogValidator,
            WorkCenterContactRepository workCenterContactRepository,
            WorkCenterContactCatalogReadPort workCenterContactCatalogReadPort
    ) {
        this.workCenterResolver = workCenterResolver;
        this.inputNormalizer = inputNormalizer;
        this.catalogValidator = catalogValidator;
        this.workCenterContactRepository = workCenterContactRepository;
        this.workCenterContactCatalogReadPort = workCenterContactCatalogReadPort;
    }

    @Override
    @Transactional
    public WorkCenterContact create(CreateWorkCenterContactCommand command) {
        String ruleSystemCode = inputNormalizer.normalizeRequiredRuleSystemCode(command.ruleSystemCode());
        String workCenterCode = inputNormalizer.normalizeRequiredWorkCenterCode(command.workCenterCode());
        String contactTypeCode = inputNormalizer.normalizeRequiredContactTypeCode(command.contactTypeCode());
        String contactValue = inputNormalizer.normalizeRequiredContactValue(command.contactValue());

        RuleEntity workCenterEntity = workCenterResolver.resolveApplicableToday(ruleSystemCode, workCenterCode);
        catalogValidator.validateContactTypeCode(ruleSystemCode, contactTypeCode, LocalDate.now());
        Integer contactNumber = workCenterContactRepository.nextContactNumberForWorkCenterRuleEntityId(workCenterEntity.getId());

        WorkCenterContact savedContact = workCenterContactRepository.save(
                workCenterEntity.getId(),
                new WorkCenterContact(contactNumber, contactTypeCode, null, contactValue)
        );

        return savedContact.withContactTypeName(
                workCenterContactCatalogReadPort.findContactTypeName(ruleSystemCode, savedContact.getContactTypeCode())
                        .orElse(null)
        );
    }
}