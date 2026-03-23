package com.b4rrhh.employee.labor_classification.infrastructure.persistence;

import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;
import com.b4rrhh.employee.labor_classification.application.port.AgreementCategoryCatalogLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class AgreementCategoryCatalogLookupAdapter implements AgreementCategoryCatalogLookupPort {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);
    private static final String BASE_QUERY = """
            select distinct
                upper(trim(cat.code)) as category_code,
                cat.name as category_name,
                cat.start_date as category_start_date,
                cat.end_date as category_end_date
            from rulesystem.agreement_category_relation r
            join rulesystem.rule_system rs
              on rs.id = r.rule_system_id
            join rulesystem.rule_entity agr
              on agr.id = r.agreement_rule_entity_id
            join rulesystem.rule_entity cat
              on cat.id = r.category_rule_entity_id
            where upper(trim(rs.code)) = :ruleSystemCode
              and upper(trim(agr.code)) = :agreementCode
              and agr.rule_entity_type_code = 'AGREEMENT'
              and cat.rule_entity_type_code = 'AGREEMENT_CATEGORY'
              and agr.rule_system_code = rs.code
              and cat.rule_system_code = rs.code
              and agr.active = true
              and cat.active = true
              and r.is_active = true
            """;

    private static final String TEMPORAL_FILTERS = """
              and agr.start_date <= :referenceDate
              and :referenceDate <= coalesce(agr.end_date, :maxDate)
              and cat.start_date <= :referenceDate
              and :referenceDate <= coalesce(cat.end_date, :maxDate)
              and r.start_date <= :referenceDate
              and :referenceDate <= coalesce(r.end_date, :maxDate)
            """;

    private static final String ORDER_BY = """
            order by category_code
            """;

    private final EntityManager entityManager;

    public AgreementCategoryCatalogLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<AgreementCategoryCatalogItem> listActiveCategoriesByAgreement(
            String ruleSystemCode,
            String agreementCode
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(BASE_QUERY + ORDER_BY)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("agreementCode", agreementCode)
                .getResultList();

        return rows.stream()
                .map(this::toCatalogItem)
                .toList();
    }

    @Override
    public List<AgreementCategoryCatalogItem> listActiveCategoriesByAgreementOnDate(
            String ruleSystemCode,
            String agreementCode,
            LocalDate referenceDate
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(BASE_QUERY + TEMPORAL_FILTERS + ORDER_BY)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("agreementCode", agreementCode)
                .setParameter("referenceDate", referenceDate)
                .setParameter("maxDate", MAX_DATE)
                .getResultList();

        return rows.stream()
                .map(this::toCatalogItem)
                .toList();
    }

    private AgreementCategoryCatalogItem toCatalogItem(Object[] row) {
        return new AgreementCategoryCatalogItem(
                String.valueOf(row[0]),
                row[1] != null ? String.valueOf(row[1]) : null,
                row[2] instanceof LocalDate ? (LocalDate) row[2] : null,
                row[3] instanceof LocalDate ? (LocalDate) row[3] : null
        );
    }
}
