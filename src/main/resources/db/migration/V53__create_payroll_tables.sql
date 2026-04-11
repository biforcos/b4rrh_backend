create schema if not exists payroll;

create table payroll.payroll (
    id bigint generated always as identity primary key,
    rule_system_code varchar(5) not null,
    employee_type_code varchar(30) not null,
    employee_number varchar(15) not null,
    payroll_period_code varchar(30) not null,
    payroll_type_code varchar(30) not null,
    presence_number integer not null,
    status varchar(30) not null,
    status_reason_code varchar(50),
    calculated_at timestamp not null,
    calculation_engine_code varchar(50) not null,
    calculation_engine_version varchar(50) not null,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now()
);

alter table payroll.payroll
    add constraint uk_payroll_business
    unique (
        rule_system_code,
        employee_type_code,
        employee_number,
        payroll_period_code,
        payroll_type_code,
        presence_number
    );

create table payroll.payroll_concept (
    id bigint generated always as identity primary key,
    payroll_id bigint not null,
    line_number integer not null,
    concept_code varchar(30) not null,
    concept_label varchar(200) not null,
    amount numeric(19, 6) not null,
    quantity numeric(19, 6),
    rate numeric(19, 6),
    concept_nature_code varchar(30) not null,
    origin_period_code varchar(30),
    display_order integer not null
);

alter table payroll.payroll_concept
    add constraint fk_payroll_concept_payroll
    foreign key (payroll_id)
    references payroll.payroll(id)
    on delete cascade;

create table payroll.payroll_context_snapshot (
    id bigint generated always as identity primary key,
    payroll_id bigint not null,
    snapshot_type_code varchar(30) not null,
    source_vertical_code varchar(30) not null,
    source_business_key_json json not null,
    snapshot_payload_json json not null
);

alter table payroll.payroll_context_snapshot
    add constraint fk_payroll_context_snapshot_payroll
    foreign key (payroll_id)
    references payroll.payroll(id)
    on delete cascade;