-- =========================================================
-- V63__create_payroll_object_binding_table.sql
-- Create payroll.payroll_object_binding
-- Binds owners (e.g., agreements) to concrete objects (e.g., salary tables)
-- =========================================================

create table payroll.payroll_object_binding (
    id bigint generated always as identity primary key,
    rule_system_code varchar(10) not null,
    owner_type_code varchar(50) not null,
    owner_code varchar(100) not null,
    binding_role_code varchar(50) not null,
    bound_object_type_code varchar(50) not null,
    bound_object_code varchar(100) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

-- Functional identity: unique binding role and owner combination
alter table payroll.payroll_object_binding
    add constraint uk_payroll_object_binding
    unique (rule_system_code, owner_type_code, owner_code, binding_role_code);

create index idx_payroll_object_binding_owner
    on payroll.payroll_object_binding (rule_system_code, owner_type_code, owner_code);

create index idx_payroll_object_binding_bound
    on payroll.payroll_object_binding (rule_system_code, bound_object_type_code, bound_object_code);

create index idx_payroll_object_binding_role
    on payroll.payroll_object_binding (rule_system_code, binding_role_code, owner_type_code, owner_code);
