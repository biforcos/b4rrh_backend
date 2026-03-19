-- =========================================================
-- V29__seed_rule_entity_and_relation_for_employee_contract.sql
-- Seed minimum rule_entity and contract_subtype_relation
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
    'CONTRACT',
    ctr.code,
    ctr.name,
    ctr.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
    ('IND', 'Indefinite Contract', 'Open-ended employment contract'),
    ('TMP', 'Temporary Contract', 'Fixed-term employment contract')
) as ctr(code, name, description)
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
    'CONTRACT_SUBTYPE',
    sub.code,
    sub.name,
    sub.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
    ('FT1', 'Full Time', 'Full-time schedule subtype'),
    ('PT1', 'Part Time', 'Part-time schedule subtype'),
    ('INT', 'Internship', 'Internship contract subtype')
) as sub(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();

insert into rulesystem.contract_subtype_relation (
    rule_system_id,
    contract_rule_entity_id,
    subtype_rule_entity_id,
    start_date,
    is_active
)
select
    rs.id,
    ctr.id,
    sub.id,
    DATE '1900-01-01',
    true
from rulesystem.rule_system rs
cross join (
    values
    ('IND', 'FT1'),
    ('IND', 'PT1'),
    ('TMP', 'FT1'),
    ('TMP', 'PT1'),
    ('TMP', 'INT')
) as rel(contract_code, subtype_code)
join rulesystem.rule_entity ctr
    on ctr.rule_system_code = rs.code
   and ctr.rule_entity_type_code = 'CONTRACT'
   and ctr.code = rel.contract_code
join rulesystem.rule_entity sub
    on sub.rule_system_code = rs.code
   and sub.rule_entity_type_code = 'CONTRACT_SUBTYPE'
   and sub.code = rel.subtype_code
where not exists (
    select 1
    from rulesystem.contract_subtype_relation existing
    where existing.rule_system_id = rs.id
      and existing.contract_rule_entity_id = ctr.id
      and existing.subtype_rule_entity_id = sub.id
      and existing.start_date = DATE '1900-01-01'
);
