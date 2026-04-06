-- =========================================================
-- V39__create_work_center_contact_table.sql
-- Create rulesystem.work_center_contact
-- =========================================================

create table rulesystem.work_center_contact (
    id bigint generated always as identity primary key,
    work_center_rule_entity_id bigint not null,
    contact_number integer not null,
    contact_type_code varchar(30) not null,
    contact_value varchar(300) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.work_center_contact
    add constraint fk_work_center_contact_rule_entity
    foreign key (work_center_rule_entity_id)
    references rulesystem.rule_entity(id);

alter table rulesystem.work_center_contact
    add constraint uk_work_center_contact_number
    unique (work_center_rule_entity_id, contact_number);

create index idx_work_center_contact_rule_entity
    on rulesystem.work_center_contact (work_center_rule_entity_id, contact_number);