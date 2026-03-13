-- =========================================================
-- V6__seed_rule_entity_type_for_employee_contact.sql
-- Seed mínimo de rule_entity_type para employee.contact
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('EMPLOYEE_CONTACT_TYPE', 'Employee Contact Type', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();
