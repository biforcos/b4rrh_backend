-- =========================================================
-- V15__seed_rule_entity_type_country.sql
-- Seed minimo de rule_entity_type para country catalog
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('COUNTRY', 'Country', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();
