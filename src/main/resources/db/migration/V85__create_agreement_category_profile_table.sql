-- =========================================================
-- V85__create_agreement_category_profile_table.sql
-- Create rulesystem.agreement_category_profile
-- =========================================================

create table rulesystem.agreement_category_profile (
    id                                    bigint generated always as identity primary key,
    agreement_category_rule_entity_id     bigint      not null,
    grupo_cotizacion_code                 varchar(2)  not null,
    tipo_nomina                           varchar(10) not null,
    created_at                            timestamp   not null default now(),
    updated_at                            timestamp   not null default now(),
    constraint uk_agreement_category_profile
        unique (agreement_category_rule_entity_id),
    constraint chk_tipo_nomina
        check (tipo_nomina in ('MENSUAL', 'DIARIO')),
    constraint fk_acp_category
        foreign key (agreement_category_rule_entity_id)
        references rulesystem.rule_entity(id)
);
