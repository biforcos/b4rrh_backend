-- =========================================================
-- V26__create_rulesystem_contract_subtype_relation_table.sql
-- Create rulesystem.contract_subtype_relation
-- =========================================================

create table rulesystem.contract_subtype_relation (
    id bigint generated always as identity primary key,
    rule_system_id bigint not null,
    contract_rule_entity_id bigint not null,
    subtype_rule_entity_id bigint not null,
    start_date date not null,
    end_date date,
    is_active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.contract_subtype_relation
    add constraint fk_contract_subtype_relation_rule_system
    foreign key (rule_system_id)
    references rulesystem.rule_system(id);

alter table rulesystem.contract_subtype_relation
    add constraint fk_contract_subtype_relation_contract_entity
    foreign key (contract_rule_entity_id)
    references rulesystem.rule_entity(id);

alter table rulesystem.contract_subtype_relation
    add constraint fk_contract_subtype_relation_subtype_entity
    foreign key (subtype_rule_entity_id)
    references rulesystem.rule_entity(id);

alter table rulesystem.contract_subtype_relation
    add constraint chk_contract_subtype_relation_dates
    check (end_date is null or start_date <= end_date);

create index idx_contract_subtype_relation_rule_system
    on rulesystem.contract_subtype_relation (rule_system_id);

create index idx_contract_subtype_relation_rule_system_entities
    on rulesystem.contract_subtype_relation (rule_system_id, contract_rule_entity_id, subtype_rule_entity_id);

create index idx_contract_subtype_relation_entities_start_date
    on rulesystem.contract_subtype_relation (contract_rule_entity_id, subtype_rule_entity_id, start_date);
