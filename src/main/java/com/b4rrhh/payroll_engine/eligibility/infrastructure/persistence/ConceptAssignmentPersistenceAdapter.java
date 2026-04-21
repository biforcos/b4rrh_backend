package com.b4rrhh.payroll_engine.eligibility.infrastructure.persistence;

import com.b4rrhh.payroll_engine.eligibility.domain.model.ConceptAssignment;
import com.b4rrhh.payroll_engine.eligibility.domain.model.EmployeeAssignmentContext;
import com.b4rrhh.payroll_engine.eligibility.domain.port.ConceptAssignmentRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ConceptAssignmentPersistenceAdapter implements ConceptAssignmentRepository {

    private final SpringDataConceptAssignmentRepository springDataRepo;

    public ConceptAssignmentPersistenceAdapter(SpringDataConceptAssignmentRepository springDataRepo) {
        this.springDataRepo = springDataRepo;
    }

    @Override
    public ConceptAssignment save(ConceptAssignment assignment) {
        ConceptAssignmentEntity entity = toEntity(assignment);
        ConceptAssignmentEntity saved = springDataRepo.save(entity);
        return toDomain(saved);
    }

    @Override
    public List<ConceptAssignment> findApplicableAssignments(EmployeeAssignmentContext context, LocalDate referenceDate) {
        return springDataRepo.findCandidates(
                context.getRuleSystemCode(),
                referenceDate,
                context.getCompanyCode(),
                context.getAgreementCode(),
                context.getEmployeeTypeCode()
        ).stream().map(this::toDomain).toList();
    }

    private ConceptAssignmentEntity toEntity(ConceptAssignment domain) {
        ConceptAssignmentEntity e = new ConceptAssignmentEntity();
        if (domain.getId() != null) {
            e.setId(domain.getId());
        }
        e.setRuleSystemCode(domain.getRuleSystemCode());
        e.setConceptCode(domain.getConceptCode());
        e.setCompanyCode(domain.getCompanyCode());
        e.setAgreementCode(domain.getAgreementCode());
        e.setEmployeeTypeCode(domain.getEmployeeTypeCode());
        e.setValidFrom(domain.getValidFrom());
        e.setValidTo(domain.getValidTo());
        e.setPriority(domain.getPriority());
        return e;
    }

    private ConceptAssignment toDomain(ConceptAssignmentEntity e) {
        return new ConceptAssignment(
                e.getId(),
                e.getRuleSystemCode(),
                e.getConceptCode(),
                e.getCompanyCode(),
                e.getAgreementCode(),
                e.getEmployeeTypeCode(),
                e.getValidFrom(),
                e.getValidTo(),
                e.getPriority(),
                e.getCreatedAt(),
                e.getUpdatedAt()
        );
    }
}
