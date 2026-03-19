-- =========================================================
-- V24__seed_rule_entity_type_for_employee_labor_classification.sql
-- Seed minimo de rule_entity_type para employee.labor_classification
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('AGREEMENT', 'Agreement', true),
    ('AGREEMENT_CATEGORY', 'Agreement Category', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();
