-- =========================================================
-- V31__create_rulesystem_resource_field_catalog_binding_table.sql
-- Create rulesystem.resource_field_catalog_binding
-- =========================================================

create table rulesystem.resource_field_catalog_binding (
    resource_code varchar(80) not null,
    field_code varchar(80) not null,
    rule_entity_type_code varchar(30),
    catalog_kind varchar(20) not null,
    depends_on_field_code varchar(80),
    custom_resolver_code varchar(80),
    active boolean not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),

    constraint pk_resource_field_catalog_binding
        primary key (resource_code, field_code),

    constraint fk_resource_field_catalog_binding_rule_entity_type
        foreign key (rule_entity_type_code)
        references rulesystem.rule_entity_type(code),

    constraint chk_resource_field_catalog_binding_kind
        check (catalog_kind in ('DIRECT', 'DEPENDENT', 'CUSTOM')),

    constraint chk_resource_field_catalog_binding_consistency
        check (
            (catalog_kind = 'DIRECT'
                and rule_entity_type_code is not null
                and depends_on_field_code is null
                and custom_resolver_code is null)
            or
            (catalog_kind = 'DEPENDENT'
                and rule_entity_type_code is not null
                and depends_on_field_code is not null
                and custom_resolver_code is null)
            or
            (catalog_kind = 'CUSTOM'
                and rule_entity_type_code is null
                and depends_on_field_code is null
                and custom_resolver_code is not null)
        )
);
