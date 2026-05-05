-- =========================================================
-- V93__seed_employee_type_catalog_binding.sql
-- Register employeeTypeCode field in resource_field_catalog_binding
-- =========================================================

insert into rulesystem.resource_field_catalog_binding (
    resource_code,
    field_code,
    rule_entity_type_code,
    catalog_kind,
    depends_on_field_code,
    custom_resolver_code,
    active
)
values ('employee', 'employeeTypeCode', 'EMPLOYEE_TYPE', 'DIRECT', null, null, true)
on conflict (resource_code, field_code) do nothing;
