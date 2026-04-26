-- =========================================================
-- V75__introduce_j01_working_time_coefficient_restructure_p01_p02.sql
--
-- Introduces J01 (COEFICIENTE_JORNADA) as a JAVA_PROVIDED
-- technical concept that carries the employee working-time
-- coefficient for the segment (0.00–1.00).
--
-- Restructures the daily-rate chain:
--   Old: SALARIO_BASE = D01 × P01  (P01 = tarifa plena desde tabla)
--   New: SALARIO_BASE = D01 × P01  (P01 = P02 × J01)
--        where P02 = tarifa plena desde tabla  (renamed from P01)
--              J01 = coeficiente de jornada    (JAVA_PROVIDED)
--              P01 = tarifa efectiva            (RATE_BY_QUANTITY)
--
-- For a full-time employee (J01=1.00): P01 = P02 × 1.00 = P02
-- For a 50%-time employee  (J01=0.50): P01 = P02 × 0.50
-- =========================================================

-- ─────────────────────────────────────────────────────────
-- 1. Rename P01 concept object_code → P02
--    (object_id is preserved; all FK references stay valid)
-- ─────────────────────────────────────────────────────────

update payroll_engine.payroll_object
set object_code = 'P02',
    updated_at  = current_timestamp
where rule_system_code = 'ESP'
  and object_type_code = 'CONCEPT'
  and object_code      = 'P01';

update payroll_engine.payroll_concept
set concept_mnemonic = 'PRECIO_DIA_PLENO',
    updated_at       = current_timestamp
where object_id = (
    select id
    from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = 'P02'
);

-- ─────────────────────────────────────────────────────────
-- 2. Rename table role and bound table codes P01_ → P02_
-- ─────────────────────────────────────────────────────────

update payroll_engine.payroll_object
set object_code = 'P02_DAILY_AMOUNT_TABLE',
    updated_at  = current_timestamp
where rule_system_code = 'ESP'
  and object_type_code = 'TABLE'
  and object_code      = 'P01_DAILY_AMOUNT_TABLE';

update payroll.payroll_object_binding
set binding_role_code  = 'P02_DAILY_AMOUNT_TABLE',
    bound_object_code  = replace(bound_object_code, 'P01_', 'P02_')
where rule_system_code = 'ESP'
  and binding_role_code = 'P01_DAILY_AMOUNT_TABLE';

update payroll.payroll_table_row
set table_code = replace(table_code, 'P01_', 'P02_')
where rule_system_code = 'ESP'
  and table_code like 'P01_%';

-- ─────────────────────────────────────────────────────────
-- 3. Register J01 and new P01 payroll objects
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code)
select seeded.rule_system_code, seeded.object_type_code, seeded.object_code
from (
    values
        ('ESP', 'CONCEPT', 'J01'),
        ('ESP', 'CONCEPT', 'P01')
) as seeded(rule_system_code, object_type_code, object_code)
where not exists (
    select 1
    from payroll_engine.payroll_object existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.object_type_code = seeded.object_type_code
      and existing.object_code      = seeded.object_code
);

-- ─────────────────────────────────────────────────────────
-- 4. Register J01 and new P01 payroll concepts
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept (
    object_id,
    concept_mnemonic,
    calculation_type,
    functional_nature,
    result_composition_mode,
    payslip_order_code,
    execution_scope
)
select
    po.id,
    seeded.concept_mnemonic,
    seeded.calculation_type,
    seeded.functional_nature,
    seeded.result_composition_mode,
    seeded.payslip_order_code,
    seeded.execution_scope
from (
    values
        ('ESP', 'J01', 'COEFICIENTE_JORNADA', 'JAVA_PROVIDED',     'TECHNICAL', 'REPLACE', null, 'PERIOD'),
        ('ESP', 'P01', 'PRECIO_DIA',           'RATE_BY_QUANTITY',  'BASE',      'REPLACE', null, 'PERIOD')
) as seeded(rule_system_code, object_code, concept_mnemonic, calculation_type, functional_nature,
            result_composition_mode, payslip_order_code, execution_scope)
join payroll_engine.payroll_object po
  on po.rule_system_code = seeded.rule_system_code
 and po.object_type_code = 'CONCEPT'
 and po.object_code      = seeded.object_code
where not exists (
    select 1
    from payroll_engine.payroll_concept existing
    where existing.object_id = po.id
);

-- ─────────────────────────────────────────────────────────
-- 5. Wire P01 operands: QUANTITY=J01, RATE=P02
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_operand (target_object_id, operand_role, source_object_id)
select
    (select id from payroll_engine.payroll_object where rule_system_code='ESP' and object_type_code='CONCEPT' and object_code='P01'),
    seeded.operand_role,
    (select id from payroll_engine.payroll_object where rule_system_code='ESP' and object_type_code='CONCEPT' and object_code=seeded.source_code)
from (values
    ('QUANTITY', 'J01'),
    ('RATE',     'P02')
) as seeded(operand_role, source_code)
where not exists (
    select 1 from payroll_engine.payroll_concept_operand existing
    where existing.target_object_id = (
        select id from payroll_engine.payroll_object
        where rule_system_code='ESP' and object_type_code='CONCEPT' and object_code='P01'
    )
    and existing.operand_role = seeded.operand_role
);

-- ─────────────────────────────────────────────────────────
-- 6. Rewire SALARIO_BASE (101) RATE operand → new P01
--    (was pointing to old P01, which is now P02)
-- ─────────────────────────────────────────────────────────

update payroll_engine.payroll_concept_operand
set source_object_id = (
    select id from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = 'P01'
)
where target_object_id = (
    select id from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = '101'
)
  and operand_role = 'RATE'
  and source_object_id = (
    select id from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = 'P02'
);
