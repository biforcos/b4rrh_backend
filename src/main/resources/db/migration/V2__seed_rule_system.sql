-- =========================================================
-- V2__seed_rule_system.sql
-- Seed mínimo de rule_system
-- =========================================================

insert into rulesystem.rule_system (
    code,
    name,
    country_code,
    active
)
values
    ('ESP', 'Spain',    'ESP', true),
    ('FRA', 'Francia',  'FRA', true),
    ('PRT', 'Portugal', 'PRT', true)
on conflict (code) do update
set
    name = excluded.name,
    country_code = excluded.country_code,
    active = excluded.active,
    updated_at = now();