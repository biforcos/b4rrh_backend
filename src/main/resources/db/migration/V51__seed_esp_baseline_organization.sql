-- =========================================================
-- V51__seed_esp_baseline_organization.sql
-- Seed coherent ESP baseline organization entities
-- =========================================================

update rulesystem.rule_entity entity
set
    name = seeded.name,
    description = seeded.description,
    active = seeded.active,
    start_date = seeded.start_date,
    end_date = seeded.end_date,
    updated_at = now()
from (
    values
        ('ESP', 'COMPANY', 'ES01', 'Spain Company 01', 'Primary baseline company for ESP flows', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'COMPANY', 'ES02', 'Spain Company 02', 'Secondary baseline company for ESP flows', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'MAIN_OFFICE', 'Main Office', 'Primary headquarters work center for ES01', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'BRANCH_NORTH', 'Branch North', 'Secondary work center for ES01', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'BRANCH_SOUTH', 'Branch South', 'Primary work center for ES02', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'BRANCH_EAST', 'Branch East', 'Secondary work center for ES02', true, DATE '1900-01-01', cast(null as date))
) as seeded(rule_system_code, rule_entity_type_code, code, name, description, active, start_date, end_date)
where entity.rule_system_code = seeded.rule_system_code
  and entity.rule_entity_type_code = seeded.rule_entity_type_code
  and entity.code = seeded.code;

insert into rulesystem.rule_entity (
    rule_system_code,
    rule_entity_type_code,
    code,
    name,
    description,
    active,
    start_date,
    end_date
)
select
    seeded.rule_system_code,
    seeded.rule_entity_type_code,
    seeded.code,
    seeded.name,
    seeded.description,
    seeded.active,
    seeded.start_date,
    seeded.end_date
from (
    values
        ('ESP', 'COMPANY', 'ES01', 'Spain Company 01', 'Primary baseline company for ESP flows', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'COMPANY', 'ES02', 'Spain Company 02', 'Secondary baseline company for ESP flows', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'MAIN_OFFICE', 'Main Office', 'Primary headquarters work center for ES01', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'BRANCH_NORTH', 'Branch North', 'Secondary work center for ES01', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'BRANCH_SOUTH', 'Branch South', 'Primary work center for ES02', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'WORK_CENTER', 'BRANCH_EAST', 'Branch East', 'Secondary work center for ES02', true, DATE '1900-01-01', cast(null as date))
) as seeded(rule_system_code, rule_entity_type_code, code, name, description, active, start_date, end_date)
where not exists (
    select 1
    from rulesystem.rule_entity existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.rule_entity_type_code = seeded.rule_entity_type_code
      and existing.code = seeded.code
);

update rulesystem.rule_entity
set
    active = false,
    updated_at = now()
where rule_system_code = 'ESP'
  and rule_entity_type_code = 'WORK_CENTER'
  and code = 'REMOTE';

delete from rulesystem.company_profile
where company_rule_entity_id in (
    select id
    from rulesystem.rule_entity
    where rule_system_code = 'ESP'
      and rule_entity_type_code = 'COMPANY'
      and code in ('ES01', 'ES02')
);

insert into rulesystem.company_profile (
    company_rule_entity_id,
    legal_name,
    tax_identifier,
    street,
    city,
    postal_code,
    region_code,
    country_code
)
select
    company_entity.id,
    seeded.legal_name,
    seeded.tax_identifier,
    seeded.street,
    seeded.city,
    seeded.postal_code,
    seeded.region_code,
    seeded.country_code
from (
    values
        ('ES01', 'B4RRHH Spain Company 01, S.L.', 'ESB4R001', 'Calle Alcala 100', 'Madrid', '28009', 'MD', 'ESP'),
        ('ES02', 'B4RRHH Spain Company 02, S.L.', 'ESB4R002', 'Avenida Diagonal 200', 'Barcelona', '08018', 'CT', 'ESP')
) as seeded(company_code, legal_name, tax_identifier, street, city, postal_code, region_code, country_code)
join rulesystem.rule_entity company_entity
  on company_entity.rule_system_code = 'ESP'
 and company_entity.rule_entity_type_code = 'COMPANY'
 and company_entity.code = seeded.company_code;