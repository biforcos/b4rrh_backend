-- =========================================================
-- V77__add_ss_irpf_concepts_700_800.sql
--
-- Adds Social Security (700) and IRPF withholding (800) deduction concepts,
-- together with their supporting structure:
--
--   B01 (BASE_COTIZABLE)   — AGGREGATE base fed by 101 (base salary)
--   P_SS  (TIPO_SS)        — JAVA_PROVIDED fixed rate: 6.35 %  (cuota obrera)
--   P_IRPF (TIPO_IRPF)     — JAVA_PROVIDED fixed rate: 15.00 % (retención provisional)
--   700 (CUOTA_SS_OBRERA)  — PERCENTAGE: B01 × P_SS  → feeds 980
--   800 (RETENCION_IRPF)   — PERCENTAGE: B01 × P_IRPF → feeds 980
--
-- Feed restructuring for 990 (LIQUIDO_A_PAGAR):
--   REMOVE: 101 → 990  (direct earning feed, replaced by 970 → 990)
--   ADD:    970 → 990  (total earnings feed net pay)
--   ADD:    980 → 990  (total deductions reduce net pay, invert_sign = true)
-- =========================================================

-- ─────────────────────────────────────────────────────────
-- 1. Payroll objects
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code)
select seeded.rule_system_code, seeded.object_type_code, seeded.object_code
from (
    values
        ('ESP', 'CONCEPT', 'B01'),
        ('ESP', 'CONCEPT', 'P_SS'),
        ('ESP', 'CONCEPT', 'P_IRPF'),
        ('ESP', 'CONCEPT', '700'),
        ('ESP', 'CONCEPT', '800')
) as seeded(rule_system_code, object_type_code, object_code)
where not exists (
    select 1 from payroll_engine.payroll_object e
    where e.rule_system_code = seeded.rule_system_code
      and e.object_type_code = seeded.object_type_code
      and e.object_code      = seeded.object_code
);

-- ─────────────────────────────────────────────────────────
-- 2. Payroll concepts
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept (
    object_id, concept_mnemonic, calculation_type, functional_nature,
    result_composition_mode, payslip_order_code, execution_scope
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
        ('ESP', 'B01',   'BASE_COTIZABLE',   'AGGREGATE',    'BASE',          'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_SS',  'TIPO_SS',          'JAVA_PROVIDED','TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_IRPF','TIPO_IRPF',        'JAVA_PROVIDED','TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', '700',   'CUOTA_SS_OBRERA',  'PERCENTAGE',   'DEDUCTION',     'REPLACE', '700',                 'PERIOD'),
        ('ESP', '800',   'RETENCION_IRPF',   'PERCENTAGE',   'DEDUCTION',     'REPLACE', '800',                 'PERIOD')
) as seeded(rule_system_code, object_code, concept_mnemonic, calculation_type, functional_nature, result_composition_mode, payslip_order_code, execution_scope)
join payroll_engine.payroll_object po
  on po.rule_system_code = seeded.rule_system_code
 and po.object_type_code = 'CONCEPT'
 and po.object_code      = seeded.object_code
where not exists (
    select 1 from payroll_engine.payroll_concept e
    where e.object_id = po.id
);

-- ─────────────────────────────────────────────────────────
-- 3. Operands for PERCENTAGE concepts
--    700: BASE = B01, PERCENTAGE = P_SS
--    800: BASE = B01, PERCENTAGE = P_IRPF
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_operand (target_object_id, operand_role, source_object_id)
select target_po.id, seeded.operand_role, source_po.id
from (
    values
        ('ESP', '700', 'BASE',       'B01'),
        ('ESP', '700', 'PERCENTAGE', 'P_SS'),
        ('ESP', '800', 'BASE',       'B01'),
        ('ESP', '800', 'PERCENTAGE', 'P_IRPF')
) as seeded(rule_system_code, target_code, operand_role, source_code)
join payroll_engine.payroll_object target_po
  on target_po.rule_system_code = seeded.rule_system_code
 and target_po.object_type_code = 'CONCEPT'
 and target_po.object_code      = seeded.target_code
join payroll_engine.payroll_object source_po
  on source_po.rule_system_code = seeded.rule_system_code
 and source_po.object_type_code = 'CONCEPT'
 and source_po.object_code      = seeded.source_code
where not exists (
    select 1 from payroll_engine.payroll_concept_operand e
    where e.target_object_id = target_po.id
      and e.operand_role     = seeded.operand_role
);

-- ─────────────────────────────────────────────────────────
-- 4. Remove direct 101 → 990 feed (replaced by 970 → 990)
-- ─────────────────────────────────────────────────────────

delete from payroll_engine.payroll_concept_feed_relation
where source_object_id = (
    select id from payroll_engine.payroll_object
    where rule_system_code = 'ESP' and object_type_code = 'CONCEPT' and object_code = '101'
)
and target_object_id = (
    select id from payroll_engine.payroll_object
    where rule_system_code = 'ESP' and object_type_code = 'CONCEPT' and object_code = '990'
);

-- ─────────────────────────────────────────────────────────
-- 5. Feed relations
--    101 → B01                (base cotizable aggregates the base salary)
--    700 → 980  invert=false  (SS feeds total deductions)
--    800 → 980  invert=false  (IRPF feeds total deductions)
--    970 → 990  invert=false  (total earnings feed net pay)
--    980 → 990  invert=true   (total deductions reduce net pay)
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_feed_relation (
    source_object_id, target_object_id, feed_mode, feed_value,
    invert_sign, effective_from, effective_to
)
select source_po.id, target_po.id, seeded.feed_mode, seeded.feed_value,
       seeded.invert_sign, seeded.effective_from, seeded.effective_to
from (
    values
        ('ESP', '101', 'B01', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date)),
        ('ESP', '700', '980', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date)),
        ('ESP', '800', '980', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date)),
        ('ESP', '970', '990', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date)),
        ('ESP', '980', '990', 'FEED_BY_SOURCE', cast(null as numeric), true,  DATE '2025-01-01', cast(null as date))
) as seeded(rule_system_code, source_code, target_code, feed_mode, feed_value, invert_sign, effective_from, effective_to)
join payroll_engine.payroll_object source_po
  on source_po.rule_system_code = seeded.rule_system_code
 and source_po.object_type_code = 'CONCEPT'
 and source_po.object_code      = seeded.source_code
join payroll_engine.payroll_object target_po
  on target_po.rule_system_code = seeded.rule_system_code
 and target_po.object_type_code = 'CONCEPT'
 and target_po.object_code      = seeded.target_code
where not exists (
    select 1 from payroll_engine.payroll_concept_feed_relation e
    where e.source_object_id = source_po.id
      and e.target_object_id = target_po.id
      and e.feed_mode        = seeded.feed_mode
      and e.effective_from   = seeded.effective_from
);

-- ─────────────────────────────────────────────────────────
-- 6. Concept assignments for 700, 800, 980
--    (primary eligible concepts — dependencies pulled in automatically)
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.concept_assignment (
    rule_system_code, concept_code, company_code, agreement_code,
    employee_type_code, valid_from, valid_to, priority
)
select seeded.rule_system_code, seeded.concept_code, seeded.company_code,
       seeded.agreement_code, seeded.employee_type_code,
       seeded.valid_from, seeded.valid_to, seeded.priority
from (
    values
        ('ESP', '700', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 700),
        ('ESP', '800', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 800),
        ('ESP', '980', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 980)
) as seeded(rule_system_code, concept_code, company_code, agreement_code, employee_type_code, valid_from, valid_to, priority)
where not exists (
    select 1 from payroll_engine.concept_assignment e
    where e.rule_system_code   = seeded.rule_system_code
      and e.concept_code       = seeded.concept_code
      and e.agreement_code     = seeded.agreement_code
      and e.valid_from         = seeded.valid_from
);

-- ─────────────────────────────────────────────────────────
-- 7. Activations for 700 and 800
--    (980 was already activated in V72)
-- ─────────────────────────────────────────────────────────

insert into payroll.payroll_object_activation (
    rule_system_code, owner_type_code, owner_code,
    target_object_type_code, target_object_code, active
)
select 'ESP', 'AGREEMENT', '99002405011982', 'PAYROLL_CONCEPT', seeded.concept_code, true
from (values ('700'), ('800')) as seeded(concept_code)
where not exists (
    select 1 from payroll.payroll_object_activation e
    where e.rule_system_code       = 'ESP'
      and e.owner_type_code        = 'AGREEMENT'
      and e.owner_code             = '99002405011982'
      and e.target_object_type_code = 'PAYROLL_CONCEPT'
      and e.target_object_code     = seeded.concept_code
);
