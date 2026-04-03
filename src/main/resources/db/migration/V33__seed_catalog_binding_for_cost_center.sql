-- =========================================================
-- V33__seed_catalog_binding_for_cost_center.sql
-- Seed resource_field_catalog_binding entry for employee.cost_center.
-- Required so the frontend can discover the catalog backing costCenterCode.
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
values
    ('employee.cost_center', 'costCenterCode', 'COST_CENTER', 'DIRECT', null, null, true)
on conflict (resource_code, field_code) do update
set
    rule_entity_type_code = excluded.rule_entity_type_code,
    catalog_kind          = excluded.catalog_kind,
    depends_on_field_code = excluded.depends_on_field_code,
    custom_resolver_code  = excluded.custom_resolver_code,
    active                = excluded.active,
    updated_at            = now();
