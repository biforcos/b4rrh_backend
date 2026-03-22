-- =========================================================
-- V32__seed_rulesystem_resource_field_catalog_binding.sql
-- Seed initial bindings for phase 1 (ADR-015)
-- =========================================================

-- Dependency note:
-- This migration seeds only resource/field bindings.
-- Required rule_entity_type base codes (for example COMPANY, WORK_CENTER,
-- EMPLOYEE_CONTACT_TYPE, EMPLOYEE_IDENTIFIER_TYPE, EMPLOYEE_ADDRESS_TYPE,
-- AGREEMENT, AGREEMENT_CATEGORY) must be provided by canonical metamodel migrations.
-- If one is missing, this migration must fail via FK constraint.

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
    ('employee.presence', 'companyCode', 'COMPANY', 'DIRECT', null, null, true),
    ('employee.work_center', 'workCenterCode', 'WORK_CENTER', 'DIRECT', null, null, true),
    ('employee.contact', 'contactTypeCode', 'EMPLOYEE_CONTACT_TYPE', 'DIRECT', null, null, true),
    ('employee.identifier', 'identifierTypeCode', 'EMPLOYEE_IDENTIFIER_TYPE', 'DIRECT', null, null, true),
    ('employee.address', 'addressTypeCode', 'EMPLOYEE_ADDRESS_TYPE', 'DIRECT', null, null, true),
    ('employee.labor_classification', 'agreementCode', 'AGREEMENT', 'DIRECT', null, null, true),
    ('employee.labor_classification', 'agreementCategoryCode', 'AGREEMENT_CATEGORY', 'DEPENDENT', 'agreementCode', null, true)
on conflict (resource_code, field_code) do update
set
    rule_entity_type_code = excluded.rule_entity_type_code,
    catalog_kind = excluded.catalog_kind,
    depends_on_field_code = excluded.depends_on_field_code,
    custom_resolver_code = excluded.custom_resolver_code,
    active = excluded.active,
    updated_at = now();
