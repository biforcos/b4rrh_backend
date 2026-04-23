-- =========================================================
-- V69__seed_payroll_object_binding_for_plus_convenio_99002405011982.sql
-- Bind agreement 99002405011982 to its agreement plus table PC_99002405011982
-- =========================================================

insert into payroll.payroll_object_binding (
    rule_system_code,
    owner_type_code,
    owner_code,
    binding_role_code,
    bound_object_type_code,
    bound_object_code,
    active
)
select
    'ESP',
    'AGREEMENT',
    '99002405011982',
    'AGREEMENT_PLUS_TABLE',
    'TABLE',
    'PC_99002405011982',
    true
where not exists (
    select 1
    from payroll.payroll_object_binding
    where rule_system_code = 'ESP'
      and owner_type_code = 'AGREEMENT'
      and owner_code = '99002405011982'
      and binding_role_code = 'AGREEMENT_PLUS_TABLE'
);
