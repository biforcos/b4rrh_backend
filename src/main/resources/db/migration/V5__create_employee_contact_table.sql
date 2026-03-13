-- =========================================================
-- V5__create_employee_contact_table.sql
-- Create employee.contact
-- =========================================================

create table employee.contact (
    id bigint generated always as identity primary key,
    employee_id bigint not null,
    contact_type_code varchar(30) not null,
    contact_value varchar(300) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.contact
    add constraint fk_contact_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.contact
    add constraint uk_contact_employee_type
    unique (employee_id, contact_type_code);
