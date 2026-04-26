-- =========================================================
-- V74__introduce_technical_concepts_d01_d02_d03.sql
--
-- Introduces JAVA_PROVIDED technical base concepts for period and
-- presence geometry. Three concepts are established:
--
--   D01 — DIAS_DEVENGO   : accrual days in segment, min(daysInSegment, 30)
--   D02 — DIAS_MES_NOMINA: payroll month convention, always 30
--   D03 — DIAS_MES_REALES: calendar days in the period month (28–31)
--
-- D01 replaces the former DIRECT_AMOUNT/CONSTANT D01 (DIAS_MES fixed at 30).
-- The D01_FIXED_30 constant feed relation is removed; D01 is now self-sufficient
-- via Java calculation. D02 and D03 are new standalone technical concepts.
-- =========================================================

-- ─────────────────────────────────────────────────────────
-- 1. Update D01: JAVA_PROVIDED / TECHNICAL
-- ─────────────────────────────────────────────────────────

update payroll_engine.payroll_concept
set calculation_type  = 'JAVA_PROVIDED',
    functional_nature = 'TECHNICAL',
    concept_mnemonic  = 'DIAS_DEVENGO',
    updated_at        = current_timestamp
where object_id = (
    select id
    from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = 'D01'
);

-- ─────────────────────────────────────────────────────────
-- 2. Remove D01_FIXED_30 constant feed relation (no longer needed)
-- ─────────────────────────────────────────────────────────

delete from payroll_engine.payroll_concept_feed_relation
where source_object_id = (
    select id
    from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONSTANT'
      and object_code      = 'D01_FIXED_30'
)
  and target_object_id = (
    select id
    from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = 'D01'
);

-- ─────────────────────────────────────────────────────────
-- 3. Register D02 and D03 payroll objects
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code)
select seeded.rule_system_code, seeded.object_type_code, seeded.object_code
from (
    values
        ('ESP', 'CONCEPT', 'D02'),
        ('ESP', 'CONCEPT', 'D03')
) as seeded(rule_system_code, object_type_code, object_code)
where not exists (
    select 1
    from payroll_engine.payroll_object existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.object_type_code = seeded.object_type_code
      and existing.object_code      = seeded.object_code
);

-- ─────────────────────────────────────────────────────────
-- 4. Register D02 and D03 payroll concepts
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
    payroll_object.id,
    seeded.concept_mnemonic,
    seeded.calculation_type,
    seeded.functional_nature,
    seeded.result_composition_mode,
    seeded.payslip_order_code,
    seeded.execution_scope
from (
    values
        ('ESP', 'D02', 'DIAS_MES_NOMINA',  'JAVA_PROVIDED', 'TECHNICAL', 'REPLACE', null, 'PERIOD'),
        ('ESP', 'D03', 'DIAS_MES_REALES',  'JAVA_PROVIDED', 'TECHNICAL', 'REPLACE', null, 'PERIOD')
) as seeded(rule_system_code, object_code, concept_mnemonic, calculation_type, functional_nature,
            result_composition_mode, payslip_order_code, execution_scope)
join payroll_engine.payroll_object payroll_object
  on payroll_object.rule_system_code = seeded.rule_system_code
 and payroll_object.object_type_code = 'CONCEPT'
 and payroll_object.object_code      = seeded.object_code
where not exists (
    select 1
    from payroll_engine.payroll_concept existing
    where existing.object_id = payroll_object.id
);

-- ─────────────────────────────────────────────────────────
-- 5. Activate D02 and D03 for agreement 99002405011982
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.concept_assignment (
    rule_system_code,
    concept_code,
    company_code,
    agreement_code,
    employee_type_code,
    valid_from,
    valid_to,
    priority,
    created_at,
    updated_at
)
select
    seeded.rule_system_code,
    seeded.concept_code,
    null,
    seeded.agreement_code,
    null,
    DATE '2025-01-01',
    null,
    seeded.priority,
    current_timestamp,
    current_timestamp
from (
    values
        ('ESP', 'D02', '99002405011982', 2),
        ('ESP', 'D03', '99002405011982', 3)
) as seeded(rule_system_code, concept_code, agreement_code, priority)
where not exists (
    select 1
    from payroll_engine.concept_assignment existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.concept_code     = seeded.concept_code
      and existing.agreement_code   = seeded.agreement_code
);
