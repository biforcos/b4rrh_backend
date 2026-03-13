-- =========================================================
-- V9__create_employee_address_table.sql
-- Create employee.address
-- =========================================================

create table employee.address (
    id bigint generated always as identity primary key,
    employee_id bigint not null,
    address_number integer not null,
    address_type_code varchar(30) not null,
    street varchar(300) not null,
    city varchar(120) not null,
    country_code varchar(3) not null,
    postal_code varchar(20),
    region_code varchar(30),
    start_date date not null,
    end_date date,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.address
    add constraint fk_address_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.address
    add constraint uk_address_number
    unique (employee_id, address_number);

alter table employee.address
    add constraint chk_address_dates
    check (end_date is null or start_date <= end_date);

create index idx_address_employee_start_date
    on employee.address (employee_id, start_date);
