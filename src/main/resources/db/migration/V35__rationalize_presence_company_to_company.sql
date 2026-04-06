-- =========================================================
-- V35__rationalize_presence_company_to_company.sql
-- Canonicalize presence company catalogs under COMPANY
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('COMPANY', 'Company', true)
on conflict (code) do update
set
    name = excluded.name,
    active = excluded.active,
    updated_at = now();

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
    legacy.rule_system_code,
    'COMPANY',
    legacy.code,
    legacy.name,
    legacy.description,
    legacy.active,
    legacy.start_date,
    legacy.end_date
from rulesystem.rule_entity legacy
where legacy.rule_entity_type_code = 'EMPLOYEE_PRESENCE_COMPANY'
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();

update rulesystem.resource_field_catalog_binding
set
    rule_entity_type_code = 'COMPANY',
    updated_at = now()
where resource_code = 'employee.presence'
  and field_code = 'companyCode'
  and rule_entity_type_code = 'EMPLOYEE_PRESENCE_COMPANY';

delete from rulesystem.rule_entity
where rule_entity_type_code = 'EMPLOYEE_PRESENCE_COMPANY';

delete from rulesystem.rule_entity_type
where code = 'EMPLOYEE_PRESENCE_COMPANY';
