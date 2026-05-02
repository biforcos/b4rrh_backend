-- =========================================================
-- V82__seed_esp_legal_contract_types.sql
-- Official Spanish contract types for rule_system ESP
-- Basis: Real Decreto-ley 32/2021 (reforma laboral), in force 2022-03-30
-- =========================================================

-- 1. CONTRACT rule_entities for ESP
insert into rulesystem.rule_entity (
    rule_system_code,
    rule_entity_type_code,
    code,
    name,
    description,
    active,
    start_date
)
values
    ('ESP', 'CONTRACT', '100', 'Indefinido ordinario (jornada completa)',  'Contrato indefinido ordinario a tiempo completo (art. 12 ET)',                                             true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '108', 'Indefinido ordinario (tiempo parcial)',     'Contrato indefinido ordinario a tiempo parcial (art. 12 ET)',                                              true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '109', 'Fijo discontinuo (jornada completa)',       'Contrato fijo discontinuo a tiempo completo (art. 16 ET)',                                                 true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '110', 'Fijo discontinuo (tiempo parcial)',         'Contrato fijo discontinuo a tiempo parcial (art. 16 ET)',                                                  true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '401', 'Temporal — circunstancias de producción',   'Temporal por circunstancias de la producción ordinarias; máx. 6 meses, ampliable a 12 por convenio (art. 15.2 ET)',   true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '402', 'Temporal — ocasional o imprevisible',       'Temporal por circunstancias de la producción ocasionales/imprevisibles; máx. 90 días/año no consecutivos (art. 15.2 ET)', true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '410', 'Sustitución con reserva de puesto',         'Sustitución de persona trabajadora con derecho a reserva de puesto (art. 15.3 ET)',                        true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '420', 'Sustitución en proceso de selección',       'Sustitución para cobertura de vacante durante proceso de selección o promoción; máx. 3 meses (art. 15.3 ET)', true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '421', 'Formativo — formación en alternancia',      'Contrato formativo en alternancia (art. 11.2 ET)',                                                         true, DATE '2022-03-30'),
    ('ESP', 'CONTRACT', '422', 'Formativo — práctica profesional',          'Contrato formativo para la obtención de la práctica profesional (art. 11.3 ET)',                           true, DATE '2022-03-30')
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name        = excluded.name,
    description = excluded.description,
    active      = excluded.active,
    start_date  = excluded.start_date,
    updated_at  = now();

-- 2. Shared subtype 01 for ESP (one placeholder per contract type, expandable later)
insert into rulesystem.rule_entity (
    rule_system_code,
    rule_entity_type_code,
    code,
    name,
    description,
    active,
    start_date
)
values
    ('ESP', 'CONTRACT_SUBTYPE', '01', 'Subtipo 01', 'Subtipo estándar', true, DATE '2022-03-30')
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name        = excluded.name,
    description = excluded.description,
    active      = excluded.active,
    start_date  = excluded.start_date,
    updated_at  = now();

-- 3. Bind each new contract type to subtype 01
insert into rulesystem.contract_subtype_relation (
    rule_system_id,
    contract_rule_entity_id,
    subtype_rule_entity_id,
    start_date,
    is_active
)
select
    rs.id,
    ctr.id,
    sub.id,
    DATE '2022-03-30',
    true
from rulesystem.rule_system rs
cross join (values
    ('100'), ('108'), ('109'), ('110'),
    ('401'), ('402'), ('410'), ('420'),
    ('421'), ('422')
) as codes(contract_code)
join rulesystem.rule_entity ctr
    on  ctr.rule_system_code      = rs.code
    and ctr.rule_entity_type_code = 'CONTRACT'
    and ctr.code                  = codes.contract_code
join rulesystem.rule_entity sub
    on  sub.rule_system_code      = rs.code
    and sub.rule_entity_type_code = 'CONTRACT_SUBTYPE'
    and sub.code                  = '01'
where rs.code = 'ESP'
  and not exists (
    select 1
    from rulesystem.contract_subtype_relation existing
    where existing.rule_system_id          = rs.id
      and existing.contract_rule_entity_id = ctr.id
      and existing.subtype_rule_entity_id  = sub.id
      and existing.start_date              = DATE '2022-03-30'
);
