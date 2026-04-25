-- =========================================================
-- V72__add_invert_sign_and_seed_aggregate_concepts_970_980_990.sql
-- 1. Add invert_sign column to payroll_concept_feed_relation
-- 2. Seed aggregate payroll concepts 970, 980, 990
-- 3. Seed feed relations from 101 to 970 and 990
-- 4. Activate 970, 980, 990 for agreement 99002405011982
-- =========================================================

-- ─────────────────────────────────────────────────────────
-- 1. Schema: add invert_sign to feed_relation
-- ─────────────────────────────────────────────────────────

alter table payroll_engine.payroll_concept_feed_relation
    add column if not exists invert_sign boolean not null default false;

-- ─────────────────────────────────────────────────────────
-- 2. Payroll objects for 970, 980, 990
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_object (
    rule_system_code,
    object_type_code,
    object_code
)
select
    seeded.rule_system_code,
    seeded.object_type_code,
    seeded.object_code
from (
    values
        ('ESP', 'CONCEPT', '970'),
        ('ESP', 'CONCEPT', '980'),
        ('ESP', 'CONCEPT', '990')
) as seeded(rule_system_code, object_type_code, object_code)
where not exists (
    select 1
    from payroll_engine.payroll_object existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.object_type_code = seeded.object_type_code
      and existing.object_code = seeded.object_code
);

-- ─────────────────────────────────────────────────────────
-- 3. Payroll concepts 970, 980, 990
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
        ('ESP', '970', 'TOTAL_DEVENGOS',    'AGGREGATE', 'TOTAL_EARNING',    'REPLACE', '970', 'PERIOD'),
        ('ESP', '980', 'TOTAL_DEDUCCIONES', 'AGGREGATE', 'TOTAL_DEDUCTION',  'REPLACE', '980', 'PERIOD'),
        ('ESP', '990', 'LIQUIDO_A_PAGAR',   'AGGREGATE', 'NET_PAY',          'REPLACE', '990', 'PERIOD')
) as seeded(rule_system_code, object_code, concept_mnemonic, calculation_type, functional_nature, result_composition_mode, payslip_order_code, execution_scope)
join payroll_engine.payroll_object payroll_object
  on payroll_object.rule_system_code = seeded.rule_system_code
 and payroll_object.object_type_code = 'CONCEPT'
 and payroll_object.object_code = seeded.object_code
where not exists (
    select 1
    from payroll_engine.payroll_concept existing
    where existing.object_id = payroll_object.id
);

-- ─────────────────────────────────────────────────────────
-- 4. Feed relations: 101 → 970 (invert=false), 101 → 990 (invert=false)
-- ─────────────────────────────────────────────────────────

insert into payroll_engine.payroll_concept_feed_relation (
    source_object_id,
    target_object_id,
    feed_mode,
    feed_value,
    invert_sign,
    effective_from,
    effective_to
)
select
    source_object.id,
    target_object.id,
    seeded.feed_mode,
    seeded.feed_value,
    seeded.invert_sign,
    seeded.effective_from,
    seeded.effective_to
from (
    values
        ('ESP', '101', '970', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date)),
        ('ESP', '101', '990', 'FEED_BY_SOURCE', cast(null as numeric), false, DATE '2025-01-01', cast(null as date))
) as seeded(rule_system_code, source_code, target_code, feed_mode, feed_value, invert_sign, effective_from, effective_to)
join payroll_engine.payroll_object source_object
    on source_object.rule_system_code = seeded.rule_system_code
   and source_object.object_type_code = 'CONCEPT'
   and source_object.object_code = seeded.source_code
join payroll_engine.payroll_object target_object
    on target_object.rule_system_code = seeded.rule_system_code
   and target_object.object_type_code = 'CONCEPT'
   and target_object.object_code = seeded.target_code
where not exists (
    select 1
    from payroll_engine.payroll_concept_feed_relation existing
    where existing.source_object_id = source_object.id
      and existing.target_object_id = target_object.id
      and existing.feed_mode = seeded.feed_mode
      and existing.effective_from = seeded.effective_from
);

-- ─────────────────────────────────────────────────────────
-- 5. Activate 970, 980, 990 for agreement 99002405011982
-- ─────────────────────────────────────────────────────────

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
    seeded.target_object_code,
    true
from (
    values ('970'), ('980'), ('990')
) as seeded(target_object_code)
where not exists (
    select 1
    from payroll.payroll_object_activation existing
    where existing.rule_system_code = 'ESP'
      and existing.owner_type_code = 'AGREEMENT'
      and existing.owner_code = '99002405011982'
      and existing.target_object_type_code = 'PAYROLL_CONCEPT'
      and existing.target_object_code = seeded.target_object_code
);
