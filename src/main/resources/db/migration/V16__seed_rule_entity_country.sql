-- =========================================================
-- V16__seed_rule_entity_country.sql
-- Seed minimo de COUNTRY rule_entity values per rule_system
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
    'COUNTRY',
    c.code,
    c.name,
    c.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('ESP', 'Spain', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('PRT', 'Portugal', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('FRA', 'France', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('DEU', 'Germany', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('ITA', 'Italy', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('GBR', 'United Kingdom', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('USA', 'United States', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('MEX', 'Mexico', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('ARG', 'Argentina', 'Country catalog based on ISO 3166-1 alpha-3'),
        ('BRA', 'Brazil', 'Country catalog based on ISO 3166-1 alpha-3')
) as c(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();
