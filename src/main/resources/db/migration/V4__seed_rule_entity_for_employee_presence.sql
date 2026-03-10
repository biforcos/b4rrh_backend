-- =========================================================
-- V4__seed_rule_entity_for_employee_presence.sql
-- Seed mínimo de rule_entity para employee.presence
-- =========================================================

insert into rulesystem.rule_entity (
    rule_system_code,
    rule_entity_type_code,
    code,
    name,
    description,
    active,
    start_date
)
values
    -- =====================================================
    -- ESP - COMPANY
    -- =====================================================
    ('ESP', 'EMPLOYEE_PRESENCE_COMPANY', 'ES01', 'Spain Company 01', 'Default company for Spain presence flows', true, DATE '1900-01-01'),

    -- =====================================================
    -- ESP - ENTRY_REASON
    -- =====================================================
    ('ESP', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'HIRING', 'Hiring', 'Initial hiring into the company', true, DATE '1900-01-01'),
    ('ESP', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'TRANSFER_IN', 'Transfer In', 'Employee transferred into this company/context', true, DATE '1900-01-01'),
    ('ESP', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'REHIRE', 'Rehire', 'Employee rehired after a previous termination', true, DATE '1900-01-01'),

    -- =====================================================
    -- ESP - EXIT_REASON
    -- =====================================================
    ('ESP', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'TERMINATION', 'Termination', 'End of employment relationship', true, DATE '1900-01-01'),
    ('ESP', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'TRANSFER_OUT', 'Transfer Out', 'Employee transferred out to another company/context', true, DATE '1900-01-01'),
    ('ESP', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'RETIREMENT', 'Retirement', 'Employee retirement', true, DATE '1900-01-01'),

    -- =====================================================
    -- FRA - COMPANY
    -- =====================================================
    ('FRA', 'EMPLOYEE_PRESENCE_COMPANY', 'FR01', 'France Company 01', 'Default company for France presence flows', true, DATE '1900-01-01'),

    -- =====================================================
    -- FRA - ENTRY_REASON
    -- =====================================================
    ('FRA', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'HIRING', 'Hiring', 'Initial hiring into the company', true, DATE '1900-01-01'),
    ('FRA', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'TRANSFER_IN', 'Transfer In', 'Employee transferred into this company/context', true, DATE '1900-01-01'),
    ('FRA', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'REHIRE', 'Rehire', 'Employee rehired after a previous termination', true, DATE '1900-01-01'),

    -- =====================================================
    -- FRA - EXIT_REASON
    -- =====================================================
    ('FRA', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'TERMINATION', 'Termination', 'End of employment relationship', true, DATE '1900-01-01'),
    ('FRA', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'TRANSFER_OUT', 'Transfer Out', 'Employee transferred out to another company/context', true, DATE '1900-01-01'),
    ('FRA', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'RETIREMENT', 'Retirement', 'Employee retirement', true, DATE '1900-01-01'),

    -- =====================================================
    -- PRT - COMPANY
    -- =====================================================
    ('PRT', 'EMPLOYEE_PRESENCE_COMPANY', 'PT01', 'Portugal Company 01', 'Default company for Portugal presence flows', true, DATE '1900-01-01'),

    -- =====================================================
    -- PRT - ENTRY_REASON
    -- =====================================================
    ('PRT', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'HIRING', 'Hiring', 'Initial hiring into the company', true, DATE '1900-01-01'),
    ('PRT', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'TRANSFER_IN', 'Transfer In', 'Employee transferred into this company/context', true, DATE '1900-01-01'),
    ('PRT', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'REHIRE', 'Rehire', 'Employee rehired after a previous termination', true, DATE '1900-01-01'),

    -- =====================================================
    -- PRT - EXIT_REASON
    -- =====================================================
    ('PRT', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'TERMINATION', 'Termination', 'End of employment relationship', true, DATE '1900-01-01'),
    ('PRT', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'TRANSFER_OUT', 'Transfer Out', 'Employee transferred out to another company/context', true, DATE '1900-01-01'),
    ('PRT', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'RETIREMENT', 'Retirement', 'Employee retirement', true, DATE '1900-01-01')
on conflict (rule_system_code, rule_entity_type_code, code) do update
set
    name = excluded.name,
    description = excluded.description,
    active = excluded.active,
    start_date = excluded.start_date,
    end_date = excluded.end_date,
    updated_at = now();