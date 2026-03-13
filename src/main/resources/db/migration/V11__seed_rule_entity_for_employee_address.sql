-- =========================================================
-- V11__seed_rule_entity_for_employee_address.sql
-- Seed minimo de rule_entity para employee.address
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
    'EMPLOYEE_ADDRESS_TYPE',
    at.code,
    at.name,
    at.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('HOME', 'Home', 'Primary residence address'),
        ('MAILING', 'Mailing', 'Mail correspondence address'),
        ('FISCAL', 'Fiscal', 'Fiscal address'),
        ('TEMPORARY', 'Temporary', 'Temporary stay address')
) as at(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();
