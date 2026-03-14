-- =========================================================
-- V19__seed_rule_entity_for_employee_work_center.sql
-- Seed mínimo de rule_entity para employee.work_center
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
    'WORK_CENTER',
    wc.code,
    wc.name,
    wc.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('MAIN_OFFICE',  'Main Office',   'Primary company headquarters work center'),
        ('BRANCH_NORTH', 'Branch North',  'Northern branch work center'),
        ('BRANCH_SOUTH', 'Branch South',  'Southern branch work center'),
        ('BRANCH_EAST',  'Branch East',   'Eastern branch work center'),
        ('REMOTE',       'Remote',        'Remote work location')
) as wc(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name        = excluded.name,
    description = excluded.description,
    active      = excluded.active,
    start_date  = excluded.start_date,
    end_date    = excluded.end_date,
    updated_at  = now();
