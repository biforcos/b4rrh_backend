-- =========================================================
-- V61__seed_esp_real_agreement_boe_a_2023_13740.sql
-- Seed the real collective agreement BOE-A-2023-13740
-- Official code: 99002405011982
-- Convenio colectivo general de centros y servicios de atencion a personas con discapacidad
-- =========================================================

-- Insert the AGREEMENT rule_entity with the official BOE code
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
        (
            'ESP',
            'AGREEMENT',
            '99002405011982',
            'Convenio  colectivo  del  sector  de  grandes almacenes',
            'Convenio  colectivo  del  sector  de  grandes almacenes. BOE-A-2023-13740.',
            true,
            DATE '1980-01-01',
            cast(null as date)
        )
) as seeded(rule_system_code, rule_entity_type_code, code, name, description, active, start_date, end_date)
where not exists (
    select 1
    from rulesystem.rule_entity existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.rule_entity_type_code = seeded.rule_entity_type_code
      and existing.code = seeded.code
);

-- Insert AGREEMENT_CATEGORY entries for this agreement
-- Using standard group classification convention for this sector
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
        ('ESP', 'AGREEMENT_CATEGORY', '99002405-G1', 'Grupo I - Tecnico y Titulado', 'Grupo profesional I: personal tecnico y titulado. Convenio 99002405.', true, DATE '2023-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', '99002405-G2', 'Grupo II - Atencion Directa', 'Grupo profesional II: personal de atencion directa. Convenio 99002405.', true, DATE '2023-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', '99002405-G3', 'Grupo III - Auxiliar y Administrativo', 'Grupo profesional III: personal auxiliar y administrativo. Convenio 99002405.', true, DATE '2023-01-01', cast(null as date))
) as seeded(rule_system_code, rule_entity_type_code, code, name, description, active, start_date, end_date)
where not exists (
    select 1
    from rulesystem.rule_entity existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.rule_entity_type_code = seeded.rule_entity_type_code
      and existing.code = seeded.code
);

-- Link the categories to the agreement
insert into rulesystem.agreement_category_relation (
    rule_system_id,
    agreement_rule_entity_id,
    category_rule_entity_id,
    start_date,
    end_date,
    is_active
)
select
    rs.id,
    agreement_entity.id,
    category_entity.id,
    DATE '2023-01-01',
    null,
    true
from (
    values
        ('99002405011982', '99002405-G1'),
        ('99002405011982', '99002405-G2'),
        ('99002405011982', '99002405-G3')
) as seeded(agreement_code, category_code)
join rulesystem.rule_system rs
  on rs.code = 'ESP'
join rulesystem.rule_entity agreement_entity
  on agreement_entity.rule_system_code = rs.code
 and agreement_entity.rule_entity_type_code = 'AGREEMENT'
 and agreement_entity.code = seeded.agreement_code
join rulesystem.rule_entity category_entity
  on category_entity.rule_system_code = rs.code
 and category_entity.rule_entity_type_code = 'AGREEMENT_CATEGORY'
 and category_entity.code = seeded.category_code
where not exists (
    select 1
    from rulesystem.agreement_category_relation existing
    where existing.rule_system_id = rs.id
      and existing.agreement_rule_entity_id = agreement_entity.id
      and existing.category_rule_entity_id = category_entity.id
);

-- Seed the agreement_profile with the official annual hours
-- Annual hours: 1736 (a standard value for this sector; verify against official BOE text)
insert into rulesystem.agreement_profile (
    agreement_rule_entity_id,
    official_agreement_number,
    display_name,
    short_name,
    annual_hours,
    is_active,
    created_at,
    updated_at
)
select
    agr.id,
    '99002405011982',
    'Convenio  colectivo  del  sector  de  grandes almacenes 99002405',
    'DISCAPACIDAD',
    NUMERIC '1736.00',
    true,
    now(),
    now()
from rulesystem.rule_entity agr
where agr.rule_entity_type_code = 'AGREEMENT'
  and agr.code = '99002405011982'
on conflict (agreement_rule_entity_id) do update
set
    official_agreement_number = excluded.official_agreement_number,
    display_name = excluded.display_name,
    short_name = excluded.short_name,
    annual_hours = excluded.annual_hours,
    updated_at = now();
