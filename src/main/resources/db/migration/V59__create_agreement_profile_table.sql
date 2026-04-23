-- =========================================================
-- V59__create_agreement_profile_table.sql
-- Create rulesystem.agreement_profile enrichment
-- =========================================================

create table rulesystem.agreement_profile (
    id bigint generated always as identity primary key,
    agreement_rule_entity_id bigint not null,
    official_agreement_number varchar(50) not null,
    display_name varchar(200) not null,
    short_name varchar(50),
    annual_hours numeric(7, 2) not null,
    is_active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.agreement_profile
    add constraint uk_agreement_profile_rule_entity
    unique (agreement_rule_entity_id);

alter table rulesystem.agreement_profile
    add constraint fk_agreement_profile_rule_entity
    foreign key (agreement_rule_entity_id)
    references rulesystem.rule_entity(id);

create index idx_agreement_profile_official_agreement_number
    on rulesystem.agreement_profile(official_agreement_number);
