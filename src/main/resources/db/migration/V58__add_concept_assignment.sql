-- V58: concept assignment table for payroll_engine eligibility

create table payroll_engine.concept_assignment (
    id                  bigint generated always as identity primary key,
    rule_system_code    varchar(30)  not null,
    concept_code        varchar(100) not null,
    company_code        varchar(30)  null,
    agreement_code      varchar(30)  null,
    employee_type_code  varchar(30)  null,
    valid_from          date         not null,
    valid_to            date         null,
    priority            int          not null,
    created_at          timestamp    not null default now(),
    updated_at          timestamp    not null default now(),

    constraint chk_concept_assignment_dates
        check (valid_to is null or valid_to >= valid_from)
);

create index idx_concept_assignment_lookup
    on payroll_engine.concept_assignment (rule_system_code, valid_from, valid_to);
