-- =========================================================
-- V17__create_employee_work_center_table.sql
-- Create employee.work_center
-- =========================================================

create table employee.work_center (
    id bigint generated always as identity primary key,
    employee_id bigint not null,
    work_center_assignment_number integer not null,
    work_center_code varchar(30) not null,
    start_date date not null,
    end_date date,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.work_center
    add constraint fk_work_center_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.work_center
    add constraint uk_work_center_assignment_number
    unique (employee_id, work_center_assignment_number);

alter table employee.work_center
    add constraint chk_work_center_dates
    check (end_date is null or start_date <= end_date);

create index idx_work_center_employee_start_date
    on employee.work_center (employee_id, start_date);
