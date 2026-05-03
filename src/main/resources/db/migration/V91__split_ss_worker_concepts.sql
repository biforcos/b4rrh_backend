-- V91__split_ss_worker_concepts.sql
--
-- Splits concept 700 (CUOTA_SS_OBRERA = B_CC × P_SS 6.35%) into two separate lines:
--   700 (CC_TRABAJADOR)       = B_CC × P_SS_CC       (4.70% — contingencias comunes)
--   703 (DESEMPLEO_TRABAJADOR) = B_CC × P_SS_DESEMPLEO (1.55% — desempleo trabajador)
--
-- Concept 701 (FP_TRABAJADOR, 0.10%) and 702 (MEI_TRABAJADOR, 0.11%) already exist from V88.
-- P_SS (6.35% all-in-one) is no longer referenced after this migration.

-- ─────────────────────────────────────────────────────────
-- 1. New payroll objects: rate concepts P_SS_CC and P_SS_DESEMPLEO, and line 703
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code)
select v.rs, v.type, v.code
from (values
    ('ESP', 'CONCEPT', 'P_SS_CC'),
    ('ESP', 'CONCEPT', 'P_SS_DESEMPLEO'),
    ('ESP', 'CONCEPT', '703')
) as v(rs, type, code)
where not exists (
    select 1 from payroll_engine.payroll_object e
    where e.rule_system_code = v.rs
      and e.object_type_code = v.type
      and e.object_code      = v.code
);

-- ─────────────────────────────────────────────────────────
-- 2. Payroll concepts for the new rate types and line 703
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept (
    object_id,
    concept_mnemonic, calculation_type, functional_nature,
    result_composition_mode, payslip_order_code, execution_scope,
    persist_to_concepts
)
select po.id, v.mnemonic, v.calc_type, v.nature, v.mode, v.order_code, v.scope, true
from (values
    ('ESP', 'P_SS_CC',        'TIPO_CC_TRABAJADOR',        'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
    ('ESP', 'P_SS_DESEMPLEO', 'TIPO_DESEMPLEO_TRABAJADOR', 'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
    ('ESP', '703',            'DESEMPLEO_TRABAJADOR',      'PERCENTAGE',      'DEDUCTION',     'REPLACE', '703',                 'PERIOD')
) as v(rs, code, mnemonic, calc_type, nature, mode, order_code, scope)
join payroll_engine.payroll_object po
    on po.rule_system_code = v.rs and po.object_code = v.code
where not exists (
    select 1 from payroll_engine.payroll_concept e
    where e.object_id = po.id
);

-- ─────────────────────────────────────────────────────────
-- 3. Rename concept 700: CUOTA_SS_OBRERA → CC_TRABAJADOR
-- ─────────────────────────────────────────────────────────

update payroll_engine.payroll_concept pc
set    concept_mnemonic = 'CC_TRABAJADOR',
       updated_at       = current_timestamp
from   payroll_engine.payroll_object po
where  pc.object_id          = po.id
  and  po.rule_system_code   = 'ESP'
  and  po.object_code        = '700'
  and  pc.concept_mnemonic   = 'CUOTA_SS_OBRERA';

-- ─────────────────────────────────────────────────────────
-- 4. Rewire concept 700: PERCENTAGE operand P_SS → P_SS_CC
-- ─────────────────────────────────────────────────────────

update payroll_engine.payroll_concept_operand
set    source_object_id = (
           select id from payroll_engine.payroll_object
           where rule_system_code = 'ESP' and object_code = 'P_SS_CC'
       ),
       updated_at = current_timestamp
where  target_object_id = (
           select id from payroll_engine.payroll_object
           where rule_system_code = 'ESP' and object_code = '700'
       )
  and  operand_role = 'PERCENTAGE'
  and  source_object_id = (
           select id from payroll_engine.payroll_object
           where rule_system_code = 'ESP' and object_code = 'P_SS'
       );

-- ─────────────────────────────────────────────────────────
-- 5. Operands for concept 703: BASE = B_CC, PERCENTAGE = P_SS_DESEMPLEO
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_operand (target_object_id, operand_role, source_object_id)
select target_po.id, v.role, source_po.id
from (values
    ('ESP', '703', 'BASE',       'B_CC'),
    ('ESP', '703', 'PERCENTAGE', 'P_SS_DESEMPLEO')
) as v(rs, target_code, role, source_code)
join payroll_engine.payroll_object target_po
    on target_po.rule_system_code = v.rs and target_po.object_code = v.target_code
join payroll_engine.payroll_object source_po
    on source_po.rule_system_code = v.rs and source_po.object_code = v.source_code
where not exists (
    select 1 from payroll_engine.payroll_concept_operand e
    where e.target_object_id = target_po.id
      and e.operand_role     = v.role
);

-- ─────────────────────────────────────────────────────────
-- 6. Feed relation: 703 → 980 (FEED_BY_SOURCE)
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_feed_relation (
    source_object_id, target_object_id,
    feed_mode, feed_value, invert_sign,
    effective_from, effective_to
)
select source_po.id, target_po.id, 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date)
from payroll_engine.payroll_object source_po
join payroll_engine.payroll_object target_po
    on target_po.rule_system_code = 'ESP' and target_po.object_code = '980'
where source_po.rule_system_code = 'ESP'
  and source_po.object_code      = '703'
  and not exists (
    select 1 from payroll_engine.payroll_concept_feed_relation e
    where e.source_object_id = source_po.id
      and e.target_object_id = target_po.id
);

-- ─────────────────────────────────────────────────────────
-- 7. Concept assignment: 703 for agreement 99002405011982
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.concept_assignment (
    rule_system_code, concept_code, company_code, agreement_code,
    employee_type_code, valid_from, valid_to, priority
)
select 'ESP', '703', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 703
where not exists (
    select 1 from payroll_engine.concept_assignment e
    where e.rule_system_code = 'ESP'
      and e.concept_code     = '703'
      and e.agreement_code   = '99002405011982'
);

-- ─────────────────────────────────────────────────────────
-- 8. Payroll object activation: 703
-- ─────────────────────────────────────────────────────────

insert into payroll.payroll_object_activation (
    rule_system_code, owner_type_code, owner_code,
    target_object_type_code, target_object_code, active
)
select 'ESP', 'AGREEMENT', '99002405011982', 'PAYROLL_CONCEPT', '703', true
where not exists (
    select 1 from payroll.payroll_object_activation e
    where e.rule_system_code        = 'ESP'
      and e.owner_type_code         = 'AGREEMENT'
      and e.owner_code              = '99002405011982'
      and e.target_object_type_code = 'PAYROLL_CONCEPT'
      and e.target_object_code      = '703'
);
