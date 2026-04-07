update rulesystem.resource_field_catalog_binding
set catalog_kind = 'CUSTOM',
    rule_entity_type_code = null,
    depends_on_field_code = null,
    custom_resolver_code = 'WORK_CENTER_BY_COMPANY',
    updated_at = now()
where resource_code = 'employee.work_center'
  and field_code = 'workCenterCode';
