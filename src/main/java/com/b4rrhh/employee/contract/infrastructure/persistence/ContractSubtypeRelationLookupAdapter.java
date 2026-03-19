package com.b4rrhh.employee.contract.infrastructure.persistence;

import com.b4rrhh.employee.contract.application.port.ContractSubtypeRelationLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ContractSubtypeRelationLookupAdapter implements ContractSubtypeRelationLookupPort {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);

    private final EntityManager entityManager;

    public ContractSubtypeRelationLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean existsActiveRelation(
            String ruleSystemCode,
            String contractCode,
            String contractSubtypeCode,
            LocalDate referenceDate
    ) {
        Object result = entityManager.createNativeQuery("""
                select case when count(*) > 0 then true else false end
                from rulesystem.contract_subtype_relation r
                join rulesystem.rule_system rs
                  on rs.id = r.rule_system_id
                join rulesystem.rule_entity agr
                  on agr.id = r.contract_rule_entity_id
                join rulesystem.rule_entity cat
                  on cat.id = r.subtype_rule_entity_id
                where upper(trim(rs.code)) = :ruleSystemCode
                  and upper(trim(agr.code)) = :contractCode
                  and upper(trim(cat.code)) = :contractSubtypeCode
                  and agr.rule_entity_type_code = 'CONTRACT'
                  and cat.rule_entity_type_code = 'CONTRACT_SUBTYPE'
                  and agr.rule_system_code = rs.code
                  and cat.rule_system_code = rs.code
                  and agr.active = true
                  and cat.active = true
                  and r.is_active = true
                  and (:referenceDate is null or agr.start_date <= :referenceDate)
                  and (:referenceDate is null or :referenceDate <= coalesce(agr.end_date, :maxDate))
                  and (:referenceDate is null or cat.start_date <= :referenceDate)
                  and (:referenceDate is null or :referenceDate <= coalesce(cat.end_date, :maxDate))
                  and (:referenceDate is null or r.start_date <= :referenceDate)
                  and (:referenceDate is null or :referenceDate <= coalesce(r.end_date, :maxDate))
                """)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("contractCode", contractCode)
                .setParameter("contractSubtypeCode", contractSubtypeCode)
                .setParameter("referenceDate", referenceDate)
                .setParameter("maxDate", MAX_DATE)
                .getSingleResult();

        if (result instanceof Boolean boolResult) {
            return boolResult;
        }
        if (result instanceof Number numberResult) {
            return numberResult.intValue() > 0;
        }

        return Boolean.parseBoolean(String.valueOf(result));
    }
}
