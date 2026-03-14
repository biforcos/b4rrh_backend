-- =========================================================
-- V12__create_employee_identifier_table.sql
-- Create employee.identifier
-- =========================================================

create table employee.identifier (
    id bigint generated always as identity primary key,
    employee_id bigint not null,
    identifier_type_code varchar(30) not null,
    identifier_value varchar(120) not null,
    issuing_country_code varchar(3),
    expiration_date date,
    is_primary boolean not null default false,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.identifier
    add constraint fk_identifier_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.identifier
    add constraint uk_identifier_employee_type
    unique (employee_id, identifier_type_code);

create index idx_identifier_employee_primary
    on employee.identifier (employee_id) where is_primary = true;