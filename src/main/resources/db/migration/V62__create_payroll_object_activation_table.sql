-- =========================================================
-- V62__create_payroll_object_activation_table.sql
-- Create payroll.payroll_object_activation
-- Defines which payroll concepts are activated by which owner (e.g., agreement)
-- =========================================================

create schema if not exists payroll;

create table payroll.payroll_object_activation (
    id bigint generated always as identity primary key,
    rule_system_code varchar(10) not null,
    owner_type_code varchar(50) not null,
    owner_code varchar(100) not null,
    target_object_type_code varchar(50) not null,
    target_object_code varchar(100) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

-- Functional identity: unique combination of system, owner, and target
alter table payroll.payroll_object_activation
    add constraint uk_payroll_object_activation
    unique (rule_system_code, owner_type_code, owner_code, target_object_type_code, target_object_code);

create index idx_payroll_object_activation_owner
    on payroll.payroll_object_activation (rule_system_code, owner_type_code, owner_code);

create index idx_payroll_object_activation_target
    on payroll.payroll_object_activation (rule_system_code, target_object_type_code, target_object_code);
