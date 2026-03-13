-- =========================================================
-- V8__add_employee_type_code_to_employee_business_key.sql
-- Add employee_type_code and migrate employee business key
-- =========================================================

alter table employee.employee
    add column employee_type_code varchar(30);

update employee.employee
set employee_type_code = 'INTERNAL'
where employee_type_code is null;

alter table employee.employee
    alter column employee_type_code set not null;

alter table employee.employee
    drop constraint if exists uk_employee_business;

alter table employee.employee
    add constraint uk_employee_business
    unique (rule_system_code, employee_type_code, employee_number);
