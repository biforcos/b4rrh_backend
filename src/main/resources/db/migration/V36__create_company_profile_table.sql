-- =========================================================
-- V36__create_company_profile_table.sql
-- Create rulesystem.company_profile
-- =========================================================

create table rulesystem.company_profile (
    id bigint generated always as identity primary key,
    company_rule_entity_id bigint not null,
    legal_name varchar(200) not null,
    tax_identifier varchar(50),
    street varchar(300),
    city varchar(120),
    postal_code varchar(20),
    region_code varchar(30),
    country_code char(3),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.company_profile
    add constraint uk_company_profile_company_rule_entity
    unique (company_rule_entity_id);

alter table rulesystem.company_profile
    add constraint fk_company_profile_company_rule_entity
    foreign key (company_rule_entity_id)
    references rulesystem.rule_entity(id);