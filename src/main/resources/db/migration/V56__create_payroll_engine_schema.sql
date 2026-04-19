create schema if not exists payroll_engine;

create table payroll_engine.payroll_object (
    id          bigint generated always as identity primary key,
    rule_system_code  varchar(10)  not null,
    object_type_code  varchar(30)  not null,
    object_code       varchar(50)  not null,
    created_at        timestamp    not null default now(),
    updated_at        timestamp    not null default now()
);

alter table payroll_engine.payroll_object
    add constraint uk_payroll_object_business_key
    unique (rule_system_code, object_type_code, object_code);

create table payroll_engine.payroll_concept (
    object_id               bigint       not null,
    concept_mnemonic        varchar(50)  not null,
    calculation_type        varchar(30)  not null,
    functional_nature       varchar(30)  not null,
    result_composition_mode varchar(30)  not null,
    payslip_order_code      varchar(30),
    execution_scope         varchar(30)  not null,
    created_at              timestamp    not null default now(),
    updated_at              timestamp    not null default now(),
    constraint pk_payroll_concept primary key (object_id),
    constraint fk_payroll_concept_object
        foreign key (object_id)
        references payroll_engine.payroll_object(id)
);

create table payroll_engine.payroll_concept_feed_relation (
    id               bigint generated always as identity primary key,
    source_object_id bigint         not null,
    target_object_id bigint         not null,
    feed_mode        varchar(30)    not null,
    feed_value       numeric(19, 6),
    effective_from   date           not null,
    effective_to     date,
    created_at       timestamp      not null default now(),
    updated_at       timestamp      not null default now(),
    constraint fk_feed_relation_source
        foreign key (source_object_id)
        references payroll_engine.payroll_object(id),
    constraint fk_feed_relation_target
        foreign key (target_object_id)
        references payroll_engine.payroll_object(id)
);
