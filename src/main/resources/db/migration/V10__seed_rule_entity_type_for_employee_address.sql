-- =========================================================
-- V10__seed_rule_entity_type_for_employee_address.sql
-- Seed minimo de rule_entity_type para employee.address
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('EMPLOYEE_ADDRESS_TYPE', 'Employee Address Type', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();
