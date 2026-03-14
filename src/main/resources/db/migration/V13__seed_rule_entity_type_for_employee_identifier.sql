-- =========================================================
-- V13__seed_rule_entity_type_for_employee_identifier.sql
-- Seed minimo de rule_entity_type para employee.identifier
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('EMPLOYEE_IDENTIFIER_TYPE', 'Employee Identifier Type', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();
