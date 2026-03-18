-- =========================================================
-- V21__seed_rule_entity_type_for_employee_cost_center.sql
-- Seed minimo de rule_entity_type y rule_entity para employee.cost_center
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('COST_CENTER', 'Cost Center', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
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
    'COST_CENTER',
    cc.code,
    cc.name,
    cc.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('CC_ADMIN', 'Administration', 'Corporate administration cost center'),
        ('CC_HR', 'Human Resources', 'Human resources operations cost center'),
        ('CC_IT', 'Information Technology', 'Technology and systems cost center'),
        ('CC_SALES', 'Sales', 'Commercial and sales activity cost center'),
        ('CC_OPER', 'Operations', 'Operations and delivery cost center')
) as cc(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();
