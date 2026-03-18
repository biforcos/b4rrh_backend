-- =========================================================
-- V20__create_employee_cost_center_table.sql
-- Create employee.cost_center
-- =========================================================

create table employee.cost_center (
    id bigint generated always as identity primary key,
    employee_id bigint not null,
    cost_center_code varchar(30) not null,
    allocation_percentage numeric(5,2) not null,
    start_date date not null,
    end_date date,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.cost_center
    add constraint fk_cost_center_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.cost_center
    add constraint uk_cost_center_business
    unique (employee_id, cost_center_code, start_date);

alter table employee.cost_center
    add constraint chk_cost_center_allocation_percentage
    check (allocation_percentage > 0 and allocation_percentage <= 100);

alter table employee.cost_center
    add constraint chk_cost_center_dates
    check (end_date is null or start_date <= end_date);

create index idx_cost_center_employee_start_date
    on employee.cost_center (employee_id, start_date);

create index idx_cost_center_employee_code_start_date
    on employee.cost_center (employee_id, cost_center_code, start_date);
