-- =========================================================
-- V23__create_employee_labor_classification_table.sql
-- Create employee.labor_classification
-- =========================================================

create table employee.labor_classification (
    id bigint generated always as identity primary key,
    employee_id bigint not null,
    agreement_code varchar(30) not null,
    agreement_category_code varchar(30) not null,
    start_date date not null,
    end_date date,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.labor_classification
    add constraint fk_labor_classification_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.labor_classification
    add constraint uk_labor_classification_business
    unique (employee_id, start_date);

alter table employee.labor_classification
    add constraint chk_labor_classification_dates
    check (end_date is null or start_date <= end_date);

create index idx_labor_classification_employee_start_date
    on employee.labor_classification (employee_id, start_date);

create index idx_labor_classification_employee_end_date
    on employee.labor_classification (employee_id, end_date);
