-- =========================================================
-- V68__seed_payroll_object_activation_for_plus_convenio_99002405011982.sql
-- Activate PLUS_CONVENIO concept for agreement 99002405011982
-- =========================================================

insert into payroll.payroll_object_activation (
    rule_system_code,
    owner_type_code,
    owner_code,
    target_object_type_code,
    target_object_code,
    active
)
select
    'ESP',
    'AGREEMENT',
    '99002405011982',
    'PAYROLL_CONCEPT',
    'PLUS_CONVENIO',
    true
where not exists (
    select 1
    from payroll.payroll_object_activation
    where rule_system_code = 'ESP'
      and owner_type_code = 'AGREEMENT'
      and owner_code = '99002405011982'
      and target_object_type_code = 'PAYROLL_CONCEPT'
      and target_object_code = 'PLUS_CONVENIO'
);
