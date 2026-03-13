-- =========================================================
-- V7__seed_rule_entity_for_employee_contact.sql
-- Seed mínimo de rule_entity para employee.contact
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
    'EMPLOYEE_CONTACT_TYPE',
    ct.code,
    ct.name,
    ct.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('EMAIL', 'Email', 'Employee email contact'),
        ('PHONE', 'Phone', 'Employee phone contact'),
        ('MOBILE', 'Mobile', 'Employee mobile contact'),
        ('COMPANY_MOBILE', 'Company Mobile', 'Company mobile assigned to employee'),
        ('EXTENSION', 'Extension', 'Employee extension contact')
) as ct(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();
