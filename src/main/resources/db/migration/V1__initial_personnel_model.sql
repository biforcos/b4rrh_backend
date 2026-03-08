-- ============================================
-- B4RRHH
-- Initial Personnel Administration Model
-- ============================================

-- =========================================================
-- RULE SYSTEM
-- =========================================================

create table rule_system (
    id bigint generated always as identity primary key,
    code varchar(5) not null,
    name varchar(100) not null,
    country_code char(3) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rule_system
    add constraint uk_rule_system_code
    unique (code);


-- =========================================================
-- RULE ENTITY TYPE
-- =========================================================

create table rule_entity_type (
    id bigint generated always as identity primary key,
    code varchar(30) not null,
    name varchar(100) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rule_entity_type
    add constraint uk_rule_entity_type_code
    unique (code);


-- =========================================================
-- RULE ENTITY
--
-- Logical identity:
--   rule_system_code + rule_entity_type_code + code
--
-- Example:
--   ESP01 + COMPANY + 009
-- =========================================================

create table rule_entity (
    id bigint generated always as identity primary key,
    rule_system_code varchar(5) not null,
    rule_entity_type_code varchar(30) not null,
    code varchar(30) not null,
    name varchar(100) not null,
    description varchar(500),
    active boolean not null default true,
    valid_from date not null,
    valid_to date,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rule_entity
    add constraint fk_rule_entity_rule_system_code
    foreign key (rule_system_code)
    references rule_system(code);

alter table rule_entity
    add constraint fk_rule_entity_type_code
    foreign key (rule_entity_type_code)
    references rule_entity_type(code);

alter table rule_entity
    add constraint uk_rule_entity_business
    unique (rule_system_code, rule_entity_type_code, code);

alter table rule_entity
    add constraint chk_rule_entity_dates
    check (valid_to is null or valid_from < valid_to);


-- =========================================================
-- EMPLOYEE
--
-- Business identity:
--   rule_system_code + employee_number
-- =========================================================

create table employee (
    id bigint generated always as identity primary key,
    rule_system_code varchar(5) not null,
    employee_number varchar(15) not null,

    first_name varchar(100) not null,
    last_name_1 varchar(100) not null,
    last_name_2 varchar(100),
    preferred_name varchar(300),

    status varchar(30) not null default 'ACTIVE',

    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee
    add constraint fk_employee_rule_system_code
    foreign key (rule_system_code)
    references rule_system(code);

alter table employee
    add constraint uk_employee_business
    unique (rule_system_code, employee_number);


-- =========================================================
-- EMPLOYEE COMPANY PRESENCE
--
-- Notes:
-- - codes are resolved through employee.rule_system_code
-- - semantic meaning of each *_code field will be defined
--   by business dictionary / application logic
-- =========================================================

create table employee_company_presence (
    id bigint generated always as identity primary key,

    employee_id bigint not null,
    presence_number integer not null,

    company_code varchar(4) not null,
    entry_reason_code varchar(30) not null,
    exit_reason_code varchar(30),

    valid_from date not null,
    valid_to date,

    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee_company_presence
    add constraint fk_presence_employee
    foreign key (employee_id)
    references employee(id);

alter table employee_company_presence
    add constraint uk_presence_number
    unique (employee_id, presence_number);

alter table employee_company_presence
    add constraint uk_presence_valid_from
    unique (employee_id, valid_from);

alter table employee_company_presence
    add constraint chk_presence_dates
    check (valid_to is null or valid_from < valid_to);


-- =========================================================
-- EMPLOYMENT CONTRACT
--
-- Notes:
-- - presence_id is optional in v0
-- - codes are resolved through employee.rule_system_code
-- - semantic meaning of each *_code field will be defined
--   by business dictionary / application logic
-- =========================================================

create table employment_contract (
    id bigint generated always as identity primary key,

    employee_id bigint not null,
    presence_id bigint,

    company_code varchar(4) not null,
    contract_type_code varchar(5) not null,

    valid_from date not null,
    valid_to date,

    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employment_contract
    add constraint fk_contract_employee
    foreign key (employee_id)
    references employee(id);

alter table employment_contract
    add constraint fk_contract_presence
    foreign key (presence_id)
    references employee_company_presence(id);

alter table employment_contract
    add constraint uk_contract_valid_from
    unique (employee_id, valid_from);

alter table employment_contract
    add constraint chk_contract_dates
    check (valid_to is null or valid_from < valid_to);