-- =========================================================
-- V3__seed_rule_entity_type_for_employee_presence.sql
-- Seed mínimo de rule_entity_type para employee.presence
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('EMPLOYEE_PRESENCE_COMPANY', 'Employee Presence Company', true),
    ('EMPLOYEE_PRESENCE_ENTRY_REASON', 'Employee Presence Entry Reason', true),
    ('EMPLOYEE_PRESENCE_EXIT_REASON', 'Employee Presence Exit Reason', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();