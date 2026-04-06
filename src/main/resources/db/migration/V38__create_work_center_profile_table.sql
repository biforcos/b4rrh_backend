-- =========================================================
-- V38__create_work_center_profile_table.sql
-- Create rulesystem.work_center_profile
-- =========================================================

create table rulesystem.work_center_profile (
    id bigint generated always as identity primary key,
    work_center_rule_entity_id bigint not null,
    company_code varchar(30),
    street varchar(300),
    city varchar(120),
    postal_code varchar(20),
    region_code varchar(30),
    country_code char(3),
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.work_center_profile
    add constraint uk_work_center_profile_rule_entity
    unique (work_center_rule_entity_id);

alter table rulesystem.work_center_profile
    add constraint fk_work_center_profile_rule_entity
    foreign key (work_center_rule_entity_id)
    references rulesystem.rule_entity(id);