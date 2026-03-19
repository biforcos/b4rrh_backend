-- =========================================================
-- V28__seed_rule_entity_type_for_employee_contract.sql
-- Seed minimum rule_entity_type for employee.contract
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('CONTRACT', 'Contract', true),
    ('CONTRACT_SUBTYPE', 'Contract Subtype', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();
