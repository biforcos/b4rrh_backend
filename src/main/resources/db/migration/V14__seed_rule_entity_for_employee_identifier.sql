-- =========================================================
-- V14__seed_rule_entity_for_employee_identifier.sql
-- Seed minimo de rule_entity para employee.identifier
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
    'EMPLOYEE_IDENTIFIER_TYPE',
    it.code,
    it.name,
    it.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('NATIONAL_ID', 'National Id', 'National identification document'),
        ('PASSPORT', 'Passport', 'Passport identification document'),
        ('TAX_ID', 'Tax Id', 'Tax identification number'),
        ('SOCIAL_SECURITY', 'Social Security', 'Social security identifier')
) as it(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();
