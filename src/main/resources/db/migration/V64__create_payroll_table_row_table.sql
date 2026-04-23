-- =========================================================
-- V64__create_payroll_table_row_table.sql
-- Create payroll.payroll_table_row
-- Defines salary table rows with search key and values
-- =========================================================

create table payroll.payroll_table_row (
    id bigint generated always as identity primary key,
    rule_system_code varchar(10) not null,
    table_code varchar(100) not null,
    search_code varchar(100) not null,
    start_date date not null,
    end_date date,
    monthly_value numeric(10, 2),
    annual_value numeric(10, 2),
    daily_value numeric(10, 2),
    hourly_value numeric(10, 2),
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

-- Functional identity: table, search key, and date range
alter table payroll.payroll_table_row
    add constraint uk_payroll_table_row
    unique (rule_system_code, table_code, search_code, start_date);

alter table payroll.payroll_table_row
    add constraint chk_payroll_table_row_dates
    check (end_date is null or start_date <= end_date);

create index idx_payroll_table_row_lookup
    on payroll.payroll_table_row (rule_system_code, table_code, search_code);

create index idx_payroll_table_row_date
    on payroll.payroll_table_row (rule_system_code, table_code, search_code, start_date, end_date);
