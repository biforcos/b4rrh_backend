-- =========================================================
-- V52__seed_esp_baseline_relationships.sql
-- Seed coherent ESP baseline relationships and profile mappings
-- =========================================================

delete from rulesystem.agreement_category_relation
where (rule_system_id, agreement_rule_entity_id, category_rule_entity_id) in (
    select
        rs.id,
        agreement_entity.id,
        category_entity.id
    from (
        values
            ('AGR_OFFICE', 'CAT_ADMIN'),
            ('AGR_TECH', 'CAT_TECH_1'),
            ('AGR_TECH', 'CAT_TECH_2')
    ) as seeded(agreement_code, category_code)
    join rulesystem.rule_system rs
      on rs.code = 'ESP'
    join rulesystem.rule_entity agreement_entity
      on agreement_entity.rule_system_code = rs.code
     and agreement_entity.rule_entity_type_code = 'AGREEMENT'
     and agreement_entity.code = seeded.agreement_code
    join rulesystem.rule_entity category_entity
      on category_entity.rule_system_code = rs.code
     and category_entity.rule_entity_type_code = 'AGREEMENT_CATEGORY'
     and category_entity.code = seeded.category_code
);

insert into rulesystem.agreement_category_relation (
    rule_system_id,
    agreement_rule_entity_id,
    category_rule_entity_id,
    start_date,
    end_date,
    is_active
)
select
    rs.id,
    agreement_entity.id,
    category_entity.id,
    DATE '1900-01-01',
    null,
    true
from (
    values
        ('AGR_OFFICE', 'CAT_ADMIN'),
        ('AGR_TECH', 'CAT_TECH_1'),
        ('AGR_TECH', 'CAT_TECH_2')
) as seeded(agreement_code, category_code)
join rulesystem.rule_system rs
  on rs.code = 'ESP'
join rulesystem.rule_entity agreement_entity
  on agreement_entity.rule_system_code = rs.code
 and agreement_entity.rule_entity_type_code = 'AGREEMENT'
 and agreement_entity.code = seeded.agreement_code
join rulesystem.rule_entity category_entity
  on category_entity.rule_system_code = rs.code
 and category_entity.rule_entity_type_code = 'AGREEMENT_CATEGORY'
 and category_entity.code = seeded.category_code;

delete from rulesystem.contract_subtype_relation
where (rule_system_id, contract_rule_entity_id, subtype_rule_entity_id) in (
    select
        rs.id,
        contract_entity.id,
        subtype_entity.id
    from (
        values
            ('IND', 'FT1'),
            ('IND', 'PT1'),
            ('TMP', 'FT1'),
            ('TMP', 'PT1'),
            ('TMP', 'INT')
    ) as seeded(contract_code, subtype_code)
    join rulesystem.rule_system rs
      on rs.code = 'ESP'
    join rulesystem.rule_entity contract_entity
      on contract_entity.rule_system_code = rs.code
     and contract_entity.rule_entity_type_code = 'CONTRACT'
     and contract_entity.code = seeded.contract_code
    join rulesystem.rule_entity subtype_entity
      on subtype_entity.rule_system_code = rs.code
     and subtype_entity.rule_entity_type_code = 'CONTRACT_SUBTYPE'
     and subtype_entity.code = seeded.subtype_code
);

insert into rulesystem.contract_subtype_relation (
    rule_system_id,
    contract_rule_entity_id,
    subtype_rule_entity_id,
    start_date,
    end_date,
    is_active
)
select
    rs.id,
    contract_entity.id,
    subtype_entity.id,
    DATE '1900-01-01',
    null,
    true
from (
    values
        ('IND', 'FT1'),
        ('IND', 'PT1'),
        ('TMP', 'FT1'),
        ('TMP', 'PT1'),
        ('TMP', 'INT')
) as seeded(contract_code, subtype_code)
join rulesystem.rule_system rs
  on rs.code = 'ESP'
join rulesystem.rule_entity contract_entity
  on contract_entity.rule_system_code = rs.code
 and contract_entity.rule_entity_type_code = 'CONTRACT'
 and contract_entity.code = seeded.contract_code
join rulesystem.rule_entity subtype_entity
  on subtype_entity.rule_system_code = rs.code
 and subtype_entity.rule_entity_type_code = 'CONTRACT_SUBTYPE'
 and subtype_entity.code = seeded.subtype_code;

delete from rulesystem.work_center_profile
where work_center_rule_entity_id in (
  select id
  from rulesystem.rule_entity
  where rule_system_code = 'ESP'
    and rule_entity_type_code = 'WORK_CENTER'
    and code in ('MAIN_OFFICE', 'BRANCH_NORTH', 'BRANCH_SOUTH', 'BRANCH_EAST')
);

insert into rulesystem.work_center_profile (
  work_center_rule_entity_id,
  company_code,
  street,
  city,
  postal_code,
  region_code,
  country_code
)
select
  work_center_entity.id,
  seeded.company_code,
  seeded.street,
  seeded.city,
  seeded.postal_code,
  seeded.region_code,
  seeded.country_code
from (
  values
    ('MAIN_OFFICE', 'ES01', 'Calle Alcala 100', 'Madrid', '28009', 'MD', 'ESP'),
    ('BRANCH_NORTH', 'ES01', 'Calle Serrano 45', 'Madrid', '28001', 'MD', 'ESP'),
    ('BRANCH_SOUTH', 'ES02', 'Avenida del Puerto 12', 'Valencia', '46021', 'VC', 'ESP'),
    ('BRANCH_EAST', 'ES02', 'Avenida Diagonal 200', 'Barcelona', '08018', 'CT', 'ESP')
) as seeded(work_center_code, company_code, street, city, postal_code, region_code, country_code)
join rulesystem.rule_entity work_center_entity
  on work_center_entity.rule_system_code = 'ESP'
 and work_center_entity.rule_entity_type_code = 'WORK_CENTER'
 and work_center_entity.code = seeded.work_center_code;