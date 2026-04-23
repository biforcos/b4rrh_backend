-- =========================================================
-- V70__seed_payroll_table_row_for_plus_convenio_pc_99002405011982.sql
-- Seed agreement plus table rows for agreement 99002405011982
-- Using representative monthly amounts per professional group
-- =========================================================

insert into payroll.payroll_table_row (
    rule_system_code,
    table_code,
    search_code,
    start_date,
    end_date,
    monthly_value,
    annual_value,
    active
)
select
    seeded.rule_system_code,
    seeded.table_code,
    seeded.search_code,
    seeded.start_date,
    seeded.end_date,
    seeded.monthly_value,
    seeded.annual_value,
    seeded.active
from (
    values
        -- Grupo I: Tecnico y Titulado
        ('ESP', 'PC_99002405011982', '99002405-G1', DATE '2023-01-01', cast(null as date), NUMERIC '250.00', NUMERIC '3000.00', true),
        -- Grupo II: Atencion Directa
        ('ESP', 'PC_99002405011982', '99002405-G2', DATE '2023-01-01', cast(null as date), NUMERIC '180.00', NUMERIC '2160.00', true),
        -- Grupo III: Auxiliar y Administrativo
        ('ESP', 'PC_99002405011982', '99002405-G3', DATE '2023-01-01', cast(null as date), NUMERIC '120.00', NUMERIC '1440.00', true)
) as seeded(rule_system_code, table_code, search_code, start_date, end_date, monthly_value, annual_value, active)
where not exists (
    select 1
    from payroll.payroll_table_row existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.table_code = seeded.table_code
      and existing.search_code = seeded.search_code
      and existing.start_date = seeded.start_date
);
