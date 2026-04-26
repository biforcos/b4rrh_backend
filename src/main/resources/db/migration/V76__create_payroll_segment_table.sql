-- =========================================================
-- V76__create_payroll_segment_table.sql
--
-- Persists the working-time segments used during payroll
-- calculation. Each row anchors one segment by its start date.
-- The segment end is implicit: it is the day before the next
-- segment's start_date, or the period end for the last segment.
-- =========================================================

create table payroll.payroll_segment (
    id            bigint generated always as identity primary key,
    payroll_id    bigint  not null,
    segment_start date    not null,
    created_at    timestamp not null default now(),

    constraint fk_payroll_segment_payroll
        foreign key (payroll_id) references payroll.payroll(id),

    constraint uk_payroll_segment
        unique (payroll_id, segment_start)
);

create index idx_payroll_segment_payroll_id
    on payroll.payroll_segment (payroll_id);
