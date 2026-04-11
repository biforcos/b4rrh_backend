alter table payroll.payroll_concept
    add constraint uk_payroll_concept_line_number
    unique (payroll_id, line_number);

alter table payroll.payroll_context_snapshot
    add constraint uk_payroll_context_snapshot_type
    unique (payroll_id, snapshot_type_code);