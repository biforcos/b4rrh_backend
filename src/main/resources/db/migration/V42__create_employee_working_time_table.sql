-- =========================================================
-- V42__create_employee_working_time_table.sql
-- Create employee.working_time
-- =========================================================

create table employee.working_time (
    id bigint generated always as identity primary key,
    employee_id bigint not null,
    working_time_number integer not null,
    start_date date not null,
    end_date date,
    working_time_percentage numeric(5,2) not null,
    weekly_hours numeric(6,2) not null,
    daily_hours numeric(6,2) not null,
    monthly_hours numeric(6,2) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.working_time
    add constraint fk_working_time_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.working_time
    add constraint uk_working_time_number
    unique (employee_id, working_time_number);

alter table employee.working_time
    add constraint chk_working_time_dates
    check (end_date is null or start_date <= end_date);

create index idx_working_time_employee_start_date
    on employee.working_time (employee_id, start_date);