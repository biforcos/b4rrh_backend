-- =========================================================
-- V50__seed_esp_baseline_catalogs.sql
-- Seed coherent ESP baseline catalogs
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
        ('ESP', 'COST_CENTER', 'CC_ADMIN', 'Administration', 'Baseline administration cost center', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'COST_CENTER', 'CC_HR', 'Human Resources', 'Baseline human resources cost center', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'COST_CENTER', 'CC_IT', 'Information Technology', 'Baseline information technology cost center', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT', 'AGR_OFFICE', 'Office Agreement', 'Baseline collective agreement for office employees', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT', 'AGR_TECH', 'Technical Agreement', 'Baseline collective agreement for technical employees', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', 'CAT_ADMIN', 'Administrative', 'Baseline agreement category for office employees', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', 'CAT_TECH_1', 'Technical Level 1', 'Baseline technical agreement category level 1', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', 'CAT_TECH_2', 'Technical Level 2', 'Baseline technical agreement category level 2', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT', 'IND', 'Indefinite Contract', 'Baseline indefinite employment contract', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT', 'TMP', 'Temporary Contract', 'Baseline temporary employment contract', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT_SUBTYPE', 'FT1', 'Full Time', 'Baseline full-time contract subtype', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT_SUBTYPE', 'PT1', 'Part Time', 'Baseline part-time contract subtype', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT_SUBTYPE', 'INT', 'Internship', 'Baseline internship contract subtype', true, DATE '1900-01-01', cast(null as date))
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
        ('ESP', 'COST_CENTER', 'CC_ADMIN', 'Administration', 'Baseline administration cost center', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'COST_CENTER', 'CC_HR', 'Human Resources', 'Baseline human resources cost center', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'COST_CENTER', 'CC_IT', 'Information Technology', 'Baseline information technology cost center', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT', 'AGR_OFFICE', 'Office Agreement', 'Baseline collective agreement for office employees', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT', 'AGR_TECH', 'Technical Agreement', 'Baseline collective agreement for technical employees', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', 'CAT_ADMIN', 'Administrative', 'Baseline agreement category for office employees', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', 'CAT_TECH_1', 'Technical Level 1', 'Baseline technical agreement category level 1', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'AGREEMENT_CATEGORY', 'CAT_TECH_2', 'Technical Level 2', 'Baseline technical agreement category level 2', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT', 'IND', 'Indefinite Contract', 'Baseline indefinite employment contract', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT', 'TMP', 'Temporary Contract', 'Baseline temporary employment contract', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT_SUBTYPE', 'FT1', 'Full Time', 'Baseline full-time contract subtype', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT_SUBTYPE', 'PT1', 'Part Time', 'Baseline part-time contract subtype', true, DATE '1900-01-01', cast(null as date)),
        ('ESP', 'CONTRACT_SUBTYPE', 'INT', 'Internship', 'Baseline internship contract subtype', true, DATE '1900-01-01', cast(null as date))
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
  and rule_entity_type_code = 'COST_CENTER'
  and code in ('CC_SALES', 'CC_OPER');