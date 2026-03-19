-- =========================================================
-- V27__evolve_employee_contract_table_for_contract_vertical.sql
-- Evolve employee.contract for employee.contract vertical
-- =========================================================

alter table employee.contract
    add column if not exists contract_subtype_code varchar(30);

update employee.contract
set contract_subtype_code = upper(trim(contract_type_code))
where contract_subtype_code is null
  and contract_type_code is not null;

alter table employee.contract
    alter column contract_subtype_code set not null;

alter table employee.contract
    drop constraint if exists fk_contract_presence;

alter table employee.contract
    drop column if exists presence_id;

alter table employee.contract
    drop column if exists company_code;

alter table employee.contract
    drop column if exists contract_type_code;

alter table employee.contract
    drop constraint if exists uk_contract_start_date;

alter table employee.contract
    drop constraint if exists uk_contract_business;

alter table employee.contract
    add constraint uk_contract_business
    unique (employee_id, start_date);

alter table employee.contract
    drop constraint if exists chk_contract_dates;

alter table employee.contract
    add constraint chk_contract_dates
    check (end_date is null or start_date <= end_date);

create index if not exists idx_contract_employee_start_date
    on employee.contract (employee_id, start_date);

create index if not exists idx_contract_employee_end_date
    on employee.contract (employee_id, end_date);
