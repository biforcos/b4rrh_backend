-- =========================================================
-- V49__seed_esp_baseline_rule_system.sql
-- Seed coherent ESP baseline rule system and employee types
-- =========================================================

update rulesystem.rule_system
set
    name = 'Spain Personnel Administration',
    country_code = 'ESP',
    active = true,
    updated_at = now()
where code = 'ESP';

insert into rulesystem.rule_system (
    code,
    name,
    country_code,
    active
)
select
    'ESP',
    'Spain Personnel Administration',
    'ESP',
    true
where not exists (
    select 1
    from rulesystem.rule_system
    where code = 'ESP'
);

update rulesystem.rule_entity_type
set
    name = 'Employee Type',
    active = true,
    updated_at = now()
where code = 'EMPLOYEE_TYPE';

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
select
    'EMPLOYEE_TYPE',
    'Employee Type',
    true
where not exists (
    select 1
    from rulesystem.rule_entity_type
    where code = 'EMPLOYEE_TYPE'
);

update rulesystem.rule_entity entity
set
    name = seeded.name,
    description = seeded.description,
    active = seeded.active,
    start_date = seeded.start_date,
    end_date = seeded.end_date,
    updated_at = now()
from (
    values
        ('ESP', 'EMPLOYEE_TYPE', 'INTERNAL', 'Internal Employee', 'Default baseline employee type for personnel administration flows', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'EMPLOYEE_TYPE', 'EXTERNAL', 'External Employee', 'Secondary baseline employee type kept available for reference data coherence', true, DATE '1900-01-01', cast(null as date))
) as seeded(rule_system_code, rule_entity_type_code, code, name, description, active, start_date, end_date)
where entity.rule_system_code = seeded.rule_system_code
  and entity.rule_entity_type_code = seeded.rule_entity_type_code
  and entity.code = seeded.code;

insert into rulesystem.rule_entity (
    rule_system_code,
    rule_entity_type_code,
    code,
    name,
    description,
    active,
    start_date,
    end_date
)
select
    seeded.rule_system_code,
    seeded.rule_entity_type_code,
    seeded.code,
    seeded.name,
    seeded.description,
    seeded.active,
    seeded.start_date,
    seeded.end_date
from (
    values
        ('ESP', 'EMPLOYEE_TYPE', 'INTERNAL', 'Internal Employee', 'Default baseline employee type for personnel administration flows', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'EMPLOYEE_TYPE', 'EXTERNAL', 'External Employee', 'Secondary baseline employee type kept available for reference data coherence', true, DATE '1900-01-01', cast(null as date))
) as seeded(rule_system_code, rule_entity_type_code, code, name, description, active, start_date, end_date)
where not exists (
    select 1
    from rulesystem.rule_entity existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.rule_entity_type_code = seeded.rule_entity_type_code
      and existing.code = seeded.code
);