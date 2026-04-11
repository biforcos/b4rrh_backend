create table payroll.calculation_run (
    id bigint generated always as identity primary key,
    rule_system_code varchar(5) not null,
    payroll_period_code varchar(30) not null,
    payroll_type_code varchar(30) not null,
    calculation_engine_code varchar(50) not null,
    calculation_engine_version varchar(50) not null,
    requested_at timestamp not null,
    requested_by varchar(100),
    status varchar(30) not null,
    target_selection_json json not null,
    total_candidates integer not null default 0,
    total_eligible integer not null default 0,
    total_claimed integer not null default 0,
    total_skipped_not_eligible integer not null default 0,
    total_skipped_already_claimed integer not null default 0,
    total_calculated integer not null default 0,
    total_not_valid integer not null default 0,
    total_errors integer not null default 0,
    started_at timestamp,
    finished_at timestamp,
    summary_json json,
    created_at timestamp not null default now(),
    updated_at timestamp not null default now(),
    constraint chk_calculation_run_counters_non_negative
        check (
            total_candidates >= 0
            and total_eligible >= 0
            and total_claimed >= 0
            and total_skipped_not_eligible >= 0
            and total_skipped_already_claimed >= 0
            and total_calculated >= 0
            and total_not_valid >= 0
            and total_errors >= 0
        ),
    constraint chk_calculation_run_finished_requires_started
        check (finished_at is null or started_at is not null)
);

create index idx_calculation_run_context
    on payroll.calculation_run (rule_system_code, payroll_period_code, payroll_type_code);

create index idx_calculation_run_status
    on payroll.calculation_run (status);

create index idx_calculation_run_requested_at
    on payroll.calculation_run (requested_at);

create table payroll.calculation_claim (
    id bigint generated always as identity primary key,
    run_id bigint not null,
    rule_system_code varchar(5) not null,
    employee_type_code varchar(30) not null,
    employee_number varchar(15) not null,
    payroll_period_code varchar(30) not null,
    payroll_type_code varchar(30) not null,
    presence_number integer not null,
    claimed_at timestamp not null,
    claimed_by varchar(100),
    constraint fk_calculation_claim_run
        foreign key (run_id)
        references payroll.calculation_run(id)
        on delete cascade,
    constraint uk_calculation_claim_business
        unique (
            rule_system_code,
            employee_type_code,
            employee_number,
            payroll_period_code,
            payroll_type_code,
            presence_number
        )
);

create index idx_calculation_claim_run_id
    on payroll.calculation_claim (run_id);

create table payroll.payroll_warning (
    id bigint generated always as identity primary key,
    payroll_id bigint not null,
    warning_code varchar(50) not null,
    severity_code varchar(20) not null,
    message varchar(500) not null,
    details_json json,
    constraint fk_payroll_warning_payroll
        foreign key (payroll_id)
        references payroll.payroll(id)
        on delete cascade
);

create index idx_payroll_warning_payroll_id
    on payroll.payroll_warning (payroll_id);

create table payroll.calculation_run_message (
    id bigint generated always as identity primary key,
    run_id bigint not null,
    message_code varchar(50) not null,
    severity_code varchar(20) not null,
    message varchar(500) not null,
    details_json json,
    rule_system_code varchar(5),
    employee_type_code varchar(30),
    employee_number varchar(15),
    payroll_period_code varchar(30),
    payroll_type_code varchar(30),
    presence_number integer,
    created_at timestamp not null default now(),
    constraint fk_calculation_run_message_run
        foreign key (run_id)
        references payroll.calculation_run(id)
        on delete cascade
);

create index idx_calculation_run_message_run_id
    on payroll.calculation_run_message (run_id);

create index idx_calculation_run_message_run_severity
    on payroll.calculation_run_message (run_id, severity_code);