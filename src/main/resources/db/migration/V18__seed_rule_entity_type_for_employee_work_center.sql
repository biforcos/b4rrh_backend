-- =========================================================
-- V18__seed_rule_entity_type_for_employee_work_center.sql
-- Seed minimo de rule_entity_type para employee.work_center
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('WORK_CENTER', 'Work Center', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();
