package com.b4rrhh.rulesystem.workcenter.application.usecase;

import com.b4rrhh.rulesystem.domain.model.RuleEntity;
import com.b4rrhh.rulesystem.workcenter.application.service.WorkCenterInputNormalizer;
import com.b4rrhh.rulesystem.workcenter.application.service.WorkCenterResolver;
import com.b4rrhh.rulesystem.workcenter.domain.exception.WorkCenterContactNotFoundException;
import com.b4rrhh.rulesystem.workcenter.domain.port.WorkCenterContactRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DeleteWorkCenterContactService implements DeleteWorkCenterContactUseCase {

    private final WorkCenterResolver workCenterResolver;
    private final WorkCenterInputNormalizer inputNormalizer;
    private final WorkCenterContactRepository workCenterContactRepository;

    public DeleteWorkCenterContactService(
            WorkCenterResolver workCenterResolver,
            WorkCenterInputNormalizer inputNormalizer,
            WorkCenterContactRepository workCenterContactRepository
    ) {
        this.workCenterResolver = workCenterResolver;
        this.inputNormalizer = inputNormalizer;
        this.workCenterContactRepository = workCenterContactRepository;
    }

    @Override
    @Transactional
    public void delete(DeleteWorkCenterContactCommand command) {
        String ruleSystemCode = inputNormalizer.normalizeRequiredRuleSystemCode(command.ruleSystemCode());
        String workCenterCode = inputNormalizer.normalizeRequiredWorkCenterCode(command.workCenterCode());
        Integer contactNumber = inputNormalizer.normalizeRequiredContactNumber(command.contactNumber());

        RuleEntity workCenterEntity = workCenterResolver.resolveApplicableToday(ruleSystemCode, workCenterCode);

        workCenterContactRepository.findByWorkCenterRuleEntityIdAndContactNumber(workCenterEntity.getId(), contactNumber)
                .orElseThrow(() -> new WorkCenterContactNotFoundException(ruleSystemCode, workCenterCode, contactNumber));

        workCenterContactRepository.deleteByWorkCenterRuleEntityIdAndContactNumber(workCenterEntity.getId(), contactNumber);
    }
}