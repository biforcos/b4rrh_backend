-- =========================================================
-- V37__rationalize_employee_contact_type_to_contact_type.sql
-- Canonicalize employee contact type catalogs under CONTACT_TYPE
-- =========================================================

insert into rulesystem.rule_entity_type (
    code,
    name,
    active
)
values
    ('CONTACT_TYPE', 'Contact Type', true)
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
    'CONTACT_TYPE',
    legacy.code,
    legacy.name,
    legacy.description,
    legacy.active,
    legacy.start_date,
    legacy.end_date
from rulesystem.rule_entity legacy
where legacy.rule_entity_type_code = 'EMPLOYEE_CONTACT_TYPE'
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
    rule_entity_type_code = 'CONTACT_TYPE',
    updated_at = now()
where resource_code = 'employee.contact'
  and field_code = 'contactTypeCode'
  and rule_entity_type_code = 'EMPLOYEE_CONTACT_TYPE';

delete from rulesystem.rule_entity
where rule_entity_type_code = 'EMPLOYEE_CONTACT_TYPE';

delete from rulesystem.rule_entity_type
where code = 'EMPLOYEE_CONTACT_TYPE';