-- =========================================================
-- V22__create_rulesystem_agreement_category_relation_table.sql
-- Create rulesystem.agreement_category_relation
-- =========================================================

create table rulesystem.agreement_category_relation (
    id bigint generated always as identity primary key,
    rule_system_id bigint not null,
    agreement_rule_entity_id bigint not null,
    category_rule_entity_id bigint not null,
    start_date date not null,
    end_date date,
    is_active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.agreement_category_relation
    add constraint fk_agreement_category_relation_rule_system
    foreign key (rule_system_id)
    references rulesystem.rule_system(id);

alter table rulesystem.agreement_category_relation
    add constraint fk_agreement_category_relation_agreement_entity
    foreign key (agreement_rule_entity_id)
    references rulesystem.rule_entity(id);

alter table rulesystem.agreement_category_relation
    add constraint fk_agreement_category_relation_category_entity
    foreign key (category_rule_entity_id)
    references rulesystem.rule_entity(id);

alter table rulesystem.agreement_category_relation
    add constraint chk_agreement_category_relation_dates
    check (end_date is null or start_date <= end_date);

create index idx_agreement_category_relation_rule_system
    on rulesystem.agreement_category_relation (rule_system_id);

create index idx_agreement_category_relation_rule_system_entities
    on rulesystem.agreement_category_relation (rule_system_id, agreement_rule_entity_id, category_rule_entity_id);

create index idx_agreement_category_relation_entities_start_date
    on rulesystem.agreement_category_relation (agreement_rule_entity_id, category_rule_entity_id, start_date);
