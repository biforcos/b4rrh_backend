package com.b4rrhh.employee.contract.infrastructure.persistence;

import com.b4rrhh.employee.contract.application.model.ContractSubtypeCatalogItem;
import com.b4rrhh.employee.contract.application.port.ContractSubtypeCatalogLookupPort;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ContractSubtypeCatalogLookupAdapter implements ContractSubtypeCatalogLookupPort {

    private static final LocalDate MAX_DATE = LocalDate.of(9999, 12, 31);
    private static final String BASE_QUERY = """
            select distinct
                upper(trim(sub.code)) as subtype_code,
                sub.name as subtype_name,
                sub.start_date as subtype_start_date,
                sub.end_date as subtype_end_date
            from rulesystem.contract_subtype_relation r
            join rulesystem.rule_system rs
              on rs.id = r.rule_system_id
            join rulesystem.rule_entity ctr
              on ctr.id = r.contract_rule_entity_id
            join rulesystem.rule_entity sub
              on sub.id = r.subtype_rule_entity_id
            where upper(trim(rs.code)) = :ruleSystemCode
              and upper(trim(ctr.code)) = :contractTypeCode
              and ctr.rule_entity_type_code = 'CONTRACT'
              and sub.rule_entity_type_code = 'CONTRACT_SUBTYPE'
              and ctr.rule_system_code = rs.code
              and sub.rule_system_code = rs.code
              and ctr.active = true
              and sub.active = true
              and r.is_active = true
            """;

    private static final String TEMPORAL_FILTERS = """
              and ctr.start_date <= :referenceDate
              and :referenceDate <= coalesce(ctr.end_date, :maxDate)
              and sub.start_date <= :referenceDate
              and :referenceDate <= coalesce(sub.end_date, :maxDate)
              and r.start_date <= :referenceDate
              and :referenceDate <= coalesce(r.end_date, :maxDate)
            """;

    private static final String ORDER_BY = """
            order by subtype_code
            """;

    private final EntityManager entityManager;

    public ContractSubtypeCatalogLookupAdapter(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<ContractSubtypeCatalogItem> listActiveSubtypesByContractType(
            String ruleSystemCode,
            String contractTypeCode
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(BASE_QUERY + ORDER_BY)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("contractTypeCode", contractTypeCode)
                .getResultList();

        return rows.stream()
                .map(this::toCatalogItem)
                .toList();
    }

    @Override
    public List<ContractSubtypeCatalogItem> listActiveSubtypesByContractTypeOnDate(
            String ruleSystemCode,
            String contractTypeCode,
            LocalDate referenceDate
    ) {
        @SuppressWarnings("unchecked")
        List<Object[]> rows = entityManager.createNativeQuery(BASE_QUERY + TEMPORAL_FILTERS + ORDER_BY)
                .setParameter("ruleSystemCode", ruleSystemCode)
                .setParameter("contractTypeCode", contractTypeCode)
                .setParameter("referenceDate", referenceDate)
                .setParameter("maxDate", MAX_DATE)
                .getResultList();

        return rows.stream()
                .map(this::toCatalogItem)
                .toList();
    }

    private ContractSubtypeCatalogItem toCatalogItem(Object[] row) {
        return new ContractSubtypeCatalogItem(
                String.valueOf(row[0]),
                row[1] != null ? String.valueOf(row[1]) : null,
                row[2] instanceof LocalDate ? (LocalDate) row[2] : null,
                row[3] instanceof LocalDate ? (LocalDate) row[3] : null
        );
    }
}