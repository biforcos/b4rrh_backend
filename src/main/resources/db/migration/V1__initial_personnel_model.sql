-- ============================================
-- B4RRHH
-- Initial Personnel Administration Model
-- ============================================

create schema if not exists rulesystem;
create schema if not exists employee;

-- =========================================================
-- RULESYSTEM.RULE_SYSTEM
-- =========================================================

create table rulesystem.rule_system (
    id bigint generated always as identity primary key,
    code varchar(5) not null,
    name varchar(100) not null,
    country_code char(3) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.rule_system
    add constraint uk_rule_system_code
    unique (code);


-- =========================================================
-- RULESYSTEM.RULE_ENTITY_TYPE
-- =========================================================

create table rulesystem.rule_entity_type (
    id bigint generated always as identity primary key,
    code varchar(30) not null,
    name varchar(100) not null,
    active boolean not null default true,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.rule_entity_type
    add constraint uk_rule_entity_type_code
    unique (code);


-- =========================================================
-- RULESYSTEM.RULE_ENTITY
-- =========================================================

create table rulesystem.rule_entity (
    id bigint generated always as identity primary key,
    rule_system_code varchar(5) not null,
    rule_entity_type_code varchar(30) not null,
    code varchar(30) not null,
    name varchar(100) not null,
    description varchar(500),
    active boolean not null default true,
    start_date date not null,
    end_date date,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table rulesystem.rule_entity
    add constraint fk_rule_entity_rule_system_code
    foreign key (rule_system_code)
    references rulesystem.rule_system(code);

alter table rulesystem.rule_entity
    add constraint fk_rule_entity_type_code
    foreign key (rule_entity_type_code)
    references rulesystem.rule_entity_type(code);

alter table rulesystem.rule_entity
    add constraint uk_rule_entity_business
    unique (rule_system_code, rule_entity_type_code, code);

alter table rulesystem.rule_entity
    add constraint chk_rule_entity_dates
    check (end_date is null or start_date < end_date);


-- =========================================================
-- EMPLOYEE.EMPLOYEE
-- =========================================================

create table employee.employee (
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

alter table employee.employee
    add constraint fk_employee_rule_system_code
    foreign key (rule_system_code)
    references rulesystem.rule_system(code);

alter table employee.employee
    add constraint uk_employee_business
    unique (rule_system_code, employee_number);


-- =========================================================
-- EMPLOYEE.PRESENCE
-- =========================================================

create table employee.presence (
    id bigint generated always as identity primary key,

    employee_id bigint not null,
    presence_number integer not null,

    company_code varchar(4) not null,
    entry_reason_code varchar(30) not null,
    exit_reason_code varchar(30),

    start_date date not null,
    end_date date,

    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.presence
    add constraint fk_presence_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.presence
    add constraint uk_presence_number
    unique (employee_id, presence_number);

alter table employee.presence
    add constraint uk_presence_start_date
    unique (employee_id, start_date);

alter table employee.presence
    add constraint chk_presence_dates
    check (end_date is null or start_date < end_date);


-- =========================================================
-- EMPLOYEE.CONTRACT
-- =========================================================

create table employee.contract (
    id bigint generated always as identity primary key,

    employee_id bigint not null,
    presence_id bigint,

    contract_code varchar(30) not null,
    company_code varchar(4) not null,
    contract_type_code varchar(5) not null,

    start_date date not null,
    end_date date,

    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table employee.contract
    add constraint fk_contract_employee
    foreign key (employee_id)
    references employee.employee(id);

alter table employee.contract
    add constraint fk_contract_presence
    foreign key (presence_id)
    references employee.presence(id);

alter table employee.contract
    add constraint uk_contract_start_date
    unique (employee_id, start_date);

alter table employee.contract
    add constraint chk_contract_dates
    check (end_date is null or start_date < end_date);