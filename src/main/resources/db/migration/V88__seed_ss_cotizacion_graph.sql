-- =========================================================
-- V88__seed_ss_cotizacion_graph.sql
--
-- Introduces the full SS cotización concept graph for agreement 99002405011982.
--
-- New tables:
--   ss_cotizacion_topes  — TGSS base limits per grupo/period_type (2025)
--   ss_cotizacion_tipos  — TGSS contribution rates per contingency (2025)
--
-- New ENGINE_PROVIDED technical rate/tope nodes:
--   P_TOPE_MAX              — max cotizable base (per employee grupo, injected by Java)
--   P_TOPE_MIN              — min cotizable base (per employee grupo, injected by Java)
--   P_FP_TRAB               — FP trabajador rate (0.10%)
--   P_MEI_TRAB              — MEI trabajador rate (0.11%)
--   P_SS_CC_EMP             — CC empresario rate (23.60%)
--   P_SS_DESEMPLEO_EMP      — Desempleo empresario rate (7.05%)
--   P_SS_FP_EMP             — FP empresario rate (0.60%)
--   P_SS_FOGASA_EMP         — FOGASA empresario rate (0.20%)
--   P_SS_MEI_EMP            — MEI empresario rate (0.58%)
--
-- New LEAST/GREATEST clamping nodes:
--   B_CC_MAX = LEAST(B01, P_TOPE_MAX)           — B01 capped at tope máximo
--   B_CC     = GREATEST(B_CC_MAX, P_TOPE_MIN)   — B_CC_MAX floored at tope mínimo
--
-- Modified:
--   700 (CUOTA_SS_OBRERA): BASE operand updated B01 → B_CC
--
-- New worker PERCENTAGE deduction concepts (→ 980):
--   701 (FP_TRABAJADOR)  = B_CC × P_FP_TRAB
--   702 (MEI_TRABAJADOR) = B_CC × P_MEI_TRAB
--
-- New employer INFORMATIONAL PERCENTAGE concepts (no feed to 980):
--   720 (SS_CC_EMPRESARIO)        = B_CC × P_SS_CC_EMP
--   721 (SS_DESEMPLEO_EMPRESARIO) = B_CC × P_SS_DESEMPLEO_EMP
--   722 (SS_FP_EMPRESARIO)        = B_CC × P_SS_FP_EMP
--   723 (SS_FOGASA_EMPRESARIO)    = B_CC × P_SS_FOGASA_EMP
--   724 (SS_MEI_EMPRESARIO)       = B_CC × P_SS_MEI_EMP
--
-- Note: concept 725 (SS_AT_EMPRESARIO) is deferred — requires CNAE tarifa per empresa.
-- =========================================================

-- ─────────────────────────────────────────────────────────
-- 1. Create ss_cotizacion_topes table
-- ─────────────────────────────────────────────────────────

create table if not exists payroll_engine.ss_cotizacion_topes (
    id               bigserial    primary key,
    rule_system_code varchar(10)  not null,
    grupo_code       varchar(5)   not null,
    period_type      varchar(10)  not null,   -- MENSUAL | DIARIO
    base_min         numeric(10,2) not null,
    base_max         numeric(10,2) not null,
    valid_from       date         not null,
    valid_to         date,
    constraint uq_ss_cotizacion_topes unique (rule_system_code, grupo_code, period_type, valid_from)
);

-- ─────────────────────────────────────────────────────────
-- 2. Seed ss_cotizacion_topes — TGSS 2025 values
--    Grupos 01-07: monthly (MENSUAL); grupos 08-11: daily (DIARIO)
--    Tope máximo único: 4,909.50 €/mes | 163.65 €/día
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.ss_cotizacion_topes
    (rule_system_code, grupo_code, period_type, base_min, base_max, valid_from)
select seeded.rule_system_code, seeded.grupo_code, seeded.period_type,
       seeded.base_min, seeded.base_max, seeded.valid_from
from (
    values
        ('ESP', '01', 'MENSUAL', 1847.40,  4909.50, DATE '2025-01-01'),
        ('ESP', '02', 'MENSUAL', 1532.10,  4909.50, DATE '2025-01-01'),
        ('ESP', '03', 'MENSUAL', 1332.90,  4909.50, DATE '2025-01-01'),
        ('ESP', '04', 'MENSUAL', 1323.00,  4909.50, DATE '2025-01-01'),
        ('ESP', '05', 'MENSUAL', 1323.00,  4909.50, DATE '2025-01-01'),
        ('ESP', '06', 'MENSUAL', 1323.00,  4909.50, DATE '2025-01-01'),
        ('ESP', '07', 'MENSUAL', 1323.00,  4909.50, DATE '2025-01-01'),
        ('ESP', '08', 'DIARIO',    44.10,   163.65, DATE '2025-01-01'),
        ('ESP', '09', 'DIARIO',    44.10,   163.65, DATE '2025-01-01'),
        ('ESP', '10', 'DIARIO',    44.10,   163.65, DATE '2025-01-01'),
        ('ESP', '11', 'DIARIO',    44.10,   163.65, DATE '2025-01-01')
) as seeded(rule_system_code, grupo_code, period_type, base_min, base_max, valid_from)
where not exists (
    select 1 from payroll_engine.ss_cotizacion_topes e
    where e.rule_system_code = seeded.rule_system_code
      and e.grupo_code       = seeded.grupo_code
      and e.period_type      = seeded.period_type
      and e.valid_from       = seeded.valid_from
);

-- ─────────────────────────────────────────────────────────
-- 3. Create ss_cotizacion_tipos table
-- ─────────────────────────────────────────────────────────

create table if not exists payroll_engine.ss_cotizacion_tipos (
    id               bigserial    primary key,
    rule_system_code varchar(10)  not null,
    contingency_code varchar(30)  not null,
    rate             numeric(6,4) not null,   -- e.g. 4.70 means 4.70%
    valid_from       date         not null,
    valid_to         date,
    constraint uq_ss_cotizacion_tipos unique (rule_system_code, contingency_code, valid_from)
);

-- ─────────────────────────────────────────────────────────
-- 4. Seed ss_cotizacion_tipos — TGSS 2025 rates
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.ss_cotizacion_tipos
    (rule_system_code, contingency_code, rate, valid_from)
select seeded.rule_system_code, seeded.contingency_code, seeded.rate, seeded.valid_from
from (
    values
        ('ESP', 'CC_TRAB',          4.70, DATE '2025-01-01'),
        ('ESP', 'DESEMPLEO_TRAB',   1.55, DATE '2025-01-01'),
        ('ESP', 'FP_TRAB',          0.10, DATE '2025-01-01'),
        ('ESP', 'MEI_TRAB',         0.11, DATE '2025-01-01'),
        ('ESP', 'CC_EMP',          23.60, DATE '2025-01-01'),
        ('ESP', 'DESEMPLEO_EMP',    7.05, DATE '2025-01-01'),
        ('ESP', 'FP_EMP',           0.60, DATE '2025-01-01'),
        ('ESP', 'FOGASA_EMP',       0.20, DATE '2025-01-01'),
        ('ESP', 'MEI_EMP',          0.58, DATE '2025-01-01')
) as seeded(rule_system_code, contingency_code, rate, valid_from)
where not exists (
    select 1 from payroll_engine.ss_cotizacion_tipos e
    where e.rule_system_code  = seeded.rule_system_code
      and e.contingency_code  = seeded.contingency_code
      and e.valid_from        = seeded.valid_from
);

-- ─────────────────────────────────────────────────────────
-- 5. Payroll objects
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_object (rule_system_code, object_type_code, object_code)
select seeded.rule_system_code, seeded.object_type_code, seeded.object_code
from (
    values
        ('ESP', 'CONCEPT', 'P_TOPE_MAX'),
        ('ESP', 'CONCEPT', 'P_TOPE_MIN'),
        ('ESP', 'CONCEPT', 'P_FP_TRAB'),
        ('ESP', 'CONCEPT', 'P_MEI_TRAB'),
        ('ESP', 'CONCEPT', 'P_SS_CC_EMP'),
        ('ESP', 'CONCEPT', 'P_SS_DESEMPLEO_EMP'),
        ('ESP', 'CONCEPT', 'P_SS_FP_EMP'),
        ('ESP', 'CONCEPT', 'P_SS_FOGASA_EMP'),
        ('ESP', 'CONCEPT', 'P_SS_MEI_EMP'),
        ('ESP', 'CONCEPT', 'B_CC_MAX'),
        ('ESP', 'CONCEPT', 'B_CC'),
        ('ESP', 'CONCEPT', '701'),
        ('ESP', 'CONCEPT', '702'),
        ('ESP', 'CONCEPT', '720'),
        ('ESP', 'CONCEPT', '721'),
        ('ESP', 'CONCEPT', '722'),
        ('ESP', 'CONCEPT', '723'),
        ('ESP', 'CONCEPT', '724')
) as seeded(rule_system_code, object_type_code, object_code)
where not exists (
    select 1 from payroll_engine.payroll_object e
    where e.rule_system_code = seeded.rule_system_code
      and e.object_type_code = seeded.object_type_code
      and e.object_code      = seeded.object_code
);

-- ─────────────────────────────────────────────────────────
-- 6. Payroll concepts
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
        -- ENGINE_PROVIDED rate/tope injectors (no payslip line)
        ('ESP', 'P_TOPE_MAX',        'TOPE_MAX_COTIZACION',      'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_TOPE_MIN',        'TOPE_MIN_COTIZACION',      'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_FP_TRAB',         'TIPO_FP_TRABAJADOR',       'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_MEI_TRAB',        'TIPO_MEI_TRABAJADOR',      'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_SS_CC_EMP',       'TIPO_CC_EMPRESARIO',       'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_SS_DESEMPLEO_EMP','TIPO_DESEMPLEO_EMPRESARIO','ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_SS_FP_EMP',       'TIPO_FP_EMPRESARIO',       'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_SS_FOGASA_EMP',   'TIPO_FOGASA_EMPRESARIO',   'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'P_SS_MEI_EMP',      'TIPO_MEI_EMPRESARIO',      'ENGINE_PROVIDED', 'TECHNICAL',     'REPLACE', cast(null as varchar), 'PERIOD'),
        -- Clamped base intermediaries (no payslip line)
        ('ESP', 'B_CC_MAX',          'BASE_COTIZACION_MAX',      'LEAST',         'BASE',          'REPLACE', cast(null as varchar), 'PERIOD'),
        ('ESP', 'B_CC',              'BASE_COTIZACION_COTIZ',    'GREATEST',      'BASE',          'REPLACE', cast(null as varchar), 'PERIOD'),
        -- Worker deductions (payslip lines 701, 702)
        ('ESP', '701',               'FP_TRABAJADOR',            'PERCENTAGE',    'DEDUCTION',     'REPLACE', '701',                 'PERIOD'),
        ('ESP', '702',               'MEI_TRABAJADOR',           'PERCENTAGE',    'DEDUCTION',     'REPLACE', '702',                 'PERIOD'),
        -- Employer costs (informational payslip lines 720-724)
        ('ESP', '720',               'SS_CC_EMPRESARIO',         'PERCENTAGE',    'INFORMATIONAL', 'REPLACE', '720',                 'PERIOD'),
        ('ESP', '721',               'SS_DESEMPLEO_EMPRESARIO',  'PERCENTAGE',    'INFORMATIONAL', 'REPLACE', '721',                 'PERIOD'),
        ('ESP', '722',               'SS_FP_EMPRESARIO',         'PERCENTAGE',    'INFORMATIONAL', 'REPLACE', '722',                 'PERIOD'),
        ('ESP', '723',               'SS_FOGASA_EMPRESARIO',     'PERCENTAGE',    'INFORMATIONAL', 'REPLACE', '723',                 'PERIOD'),
        ('ESP', '724',               'SS_MEI_EMPRESARIO',        'PERCENTAGE',    'INFORMATIONAL', 'REPLACE', '724',                 'PERIOD')
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
-- 7. Operands
--    B_CC_MAX : LEFT = B01,      RIGHT = P_TOPE_MAX
--    B_CC     : LEFT = B_CC_MAX, RIGHT = P_TOPE_MIN
--    701      : BASE = B_CC,     PERCENTAGE = P_FP_TRAB
--    702      : BASE = B_CC,     PERCENTAGE = P_MEI_TRAB
--    720      : BASE = B_CC,     PERCENTAGE = P_SS_CC_EMP
--    721      : BASE = B_CC,     PERCENTAGE = P_SS_DESEMPLEO_EMP
--    722      : BASE = B_CC,     PERCENTAGE = P_SS_FP_EMP
--    723      : BASE = B_CC,     PERCENTAGE = P_SS_FOGASA_EMP
--    724      : BASE = B_CC,     PERCENTAGE = P_SS_MEI_EMP
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_operand (target_object_id, operand_role, source_object_id)
select target_po.id, seeded.operand_role, source_po.id
from (
    values
        ('ESP', 'B_CC_MAX', 'LEFT',       'B01'),
        ('ESP', 'B_CC_MAX', 'RIGHT',      'P_TOPE_MAX'),
        ('ESP', 'B_CC',     'LEFT',       'B_CC_MAX'),
        ('ESP', 'B_CC',     'RIGHT',      'P_TOPE_MIN'),
        ('ESP', '701',      'BASE',       'B_CC'),
        ('ESP', '701',      'PERCENTAGE', 'P_FP_TRAB'),
        ('ESP', '702',      'BASE',       'B_CC'),
        ('ESP', '702',      'PERCENTAGE', 'P_MEI_TRAB'),
        ('ESP', '720',      'BASE',       'B_CC'),
        ('ESP', '720',      'PERCENTAGE', 'P_SS_CC_EMP'),
        ('ESP', '721',      'BASE',       'B_CC'),
        ('ESP', '721',      'PERCENTAGE', 'P_SS_DESEMPLEO_EMP'),
        ('ESP', '722',      'BASE',       'B_CC'),
        ('ESP', '722',      'PERCENTAGE', 'P_SS_FP_EMP'),
        ('ESP', '723',      'BASE',       'B_CC'),
        ('ESP', '723',      'PERCENTAGE', 'P_SS_FOGASA_EMP'),
        ('ESP', '724',      'BASE',       'B_CC'),
        ('ESP', '724',      'PERCENTAGE', 'P_SS_MEI_EMP')
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
-- 8. Update 700 BASE operand: B01 → B_CC
--    (700 now applies to the clamped base, not the raw aggregate)
-- ─────────────────────────────────────────────────────────

update payroll_engine.payroll_concept_operand
set source_object_id = (
    select id from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = 'B_CC'
)
where target_object_id = (
    select id from payroll_engine.payroll_object
    where rule_system_code = 'ESP'
      and object_type_code = 'CONCEPT'
      and object_code      = '700'
)
  and operand_role = 'BASE';

-- ─────────────────────────────────────────────────────────
-- 9. Feed relations
--    701 → 980  (FP worker deduction)
--    702 → 980  (MEI worker deduction)
--    Employer concepts 720-724: informational — no feed to 980
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_feed_relation (
    source_object_id, target_object_id, feed_mode, feed_value,
    invert_sign, effective_from, effective_to
)
select source_po.id, target_po.id, seeded.feed_mode, seeded.feed_value,
       seeded.invert_sign, seeded.effective_from, seeded.effective_to
from (
    values
        ('ESP', '701', '980', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date)),
        ('ESP', '702', '980', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date))
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
-- 10. Concept assignments
--     Worker deductions (701, 702) and employer costs (720-724)
--     are all assigned to agreement 99002405011982.
--     Dependencies (P_*, B_CC_MAX, B_CC, B01) are resolved automatically.
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
        ('ESP', '701', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 701),
        ('ESP', '702', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 702),
        ('ESP', '720', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 720),
        ('ESP', '721', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 721),
        ('ESP', '722', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 722),
        ('ESP', '723', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 723),
        ('ESP', '724', cast(null as varchar), '99002405011982', cast(null as varchar), DATE '2025-01-01', cast(null as date), 724)
) as seeded(rule_system_code, concept_code, company_code, agreement_code, employee_type_code, valid_from, valid_to, priority)
where not exists (
    select 1 from payroll_engine.concept_assignment e
    where e.rule_system_code = seeded.rule_system_code
      and e.concept_code     = seeded.concept_code
      and e.agreement_code   = seeded.agreement_code
      and e.valid_from       = seeded.valid_from
);

-- ─────────────────────────────────────────────────────────
-- 11. Payroll object activations
--     Worker: 701, 702
--     Employer (informational): 720, 721, 722, 723, 724
-- ─────────────────────────────────────────────────────────

insert into payroll.payroll_object_activation (
    rule_system_code, owner_type_code, owner_code,
    target_object_type_code, target_object_code, active
)
select 'ESP', 'AGREEMENT', '99002405011982', 'PAYROLL_CONCEPT', seeded.concept_code, true
from (
    values ('701'), ('702'), ('720'), ('721'), ('722'), ('723'), ('724')
) as seeded(concept_code)
where not exists (
    select 1 from payroll.payroll_object_activation e
    where e.rule_system_code        = 'ESP'
      and e.owner_type_code         = 'AGREEMENT'
      and e.owner_code              = '99002405011982'
      and e.target_object_type_code = 'PAYROLL_CONCEPT'
      and e.target_object_code      = seeded.concept_code
);
