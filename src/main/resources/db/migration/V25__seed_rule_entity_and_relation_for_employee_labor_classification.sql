-- =========================================================
-- V25__seed_rule_entity_and_relation_for_employee_labor_classification.sql
-- Seed minimo de rule_entity y agreement_category_relation
-- =========================================================

insert into rulesystem.rule_entity (
    rule_system_code,
    rule_entity_type_code,
    code,
    name,
    description,
    active,
    start_date
)
select
    rs.code,
    'AGREEMENT',
    agr.code,
    agr.name,
    agr.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('AGR_OFFICE', 'Office Agreement', 'Collective agreement for office employees'),
        ('AGR_TECH', 'Technical Agreement', 'Collective agreement for technical employees')
) as agr(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();

insert into rulesystem.rule_entity (
    rule_system_code,
    rule_entity_type_code,
    code,
    name,
    description,
    active,
    start_date
)
select
    rs.code,
    'AGREEMENT_CATEGORY',
    cat.code,
    cat.name,
    cat.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('CAT_ADMIN', 'Administrative', 'Administrative labor category'),
        ('CAT_TECH_1', 'Technical Level 1', 'Technical labor category level 1'),
        ('CAT_TECH_2', 'Technical Level 2', 'Technical labor category level 2')
) as cat(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();

insert into rulesystem.agreement_category_relation (
    rule_system_id,
    agreement_rule_entity_id,
    category_rule_entity_id,
    start_date,
    is_active
)
select
    rs.id,
    agr.id,
    cat.id,
    DATE '1900-01-01',
    true
from rulesystem.rule_system rs
cross join (
    values
        ('AGR_OFFICE', 'CAT_ADMIN'),
        ('AGR_TECH', 'CAT_TECH_1'),
        ('AGR_TECH', 'CAT_TECH_2')
) as rel(agreement_code, category_code)
join rulesystem.rule_entity agr
    on agr.rule_system_code = rs.code
   and agr.rule_entity_type_code = 'AGREEMENT'
   and agr.code = rel.agreement_code
join rulesystem.rule_entity cat
    on cat.rule_system_code = rs.code
   and cat.rule_entity_type_code = 'AGREEMENT_CATEGORY'
   and cat.code = rel.category_code
where not exists (
    select 1
    from rulesystem.agreement_category_relation existing
    where existing.rule_system_id = rs.id
      and existing.agreement_rule_entity_id = agr.id
      and existing.category_rule_entity_id = cat.id
      and existing.start_date = DATE '1900-01-01'
);
