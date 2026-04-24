-- =========================================================
-- V71__seed_minimal_real_payroll_concepts_101_d01_p01.sql
-- Seed minimal real payroll concept graph for concept 101 = D01 x P01
-- =========================================================

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
        ('ESP', 'CONCEPT', '101'),
        ('ESP', 'CONCEPT', 'D01'),
    ('ESP', 'CONCEPT', 'P01'),
    ('ESP', 'CONSTANT', 'D01_FIXED_30'),
    ('ESP', 'TABLE', 'P01_DAILY_AMOUNT_TABLE')
) as seeded(rule_system_code, object_type_code, object_code)
where not exists (
    select 1
    from payroll_engine.payroll_object existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.object_type_code = seeded.object_type_code
      and existing.object_code = seeded.object_code
);

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
        ('ESP', '101', 'SALARIO_BASE', 'RATE_BY_QUANTITY', 'EARNING', 'REPLACE', '101', 'PERIOD'),
        ('ESP', 'D01', 'DIAS_MES', 'DIRECT_AMOUNT', 'INFORMATIONAL', 'REPLACE', null, 'PERIOD'),
        ('ESP', 'P01', 'PRECIO_DIA', 'DIRECT_AMOUNT', 'BASE', 'REPLACE', null, 'PERIOD')
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

insert into payroll_engine.payroll_concept_operand (
    target_object_id,
    operand_role,
    source_object_id
)
select
    target_object.id,
    seeded.operand_role,
    source_object.id
from (
    values
        ('ESP', '101', 'QUANTITY', 'D01'),
        ('ESP', '101', 'RATE', 'P01')
) as seeded(rule_system_code, target_object_code, operand_role, source_object_code)
join payroll_engine.payroll_object target_object
  on target_object.rule_system_code = seeded.rule_system_code
 and target_object.object_type_code = 'CONCEPT'
 and target_object.object_code = seeded.target_object_code
join payroll_engine.payroll_object source_object
  on source_object.rule_system_code = seeded.rule_system_code
 and source_object.object_type_code = 'CONCEPT'
 and source_object.object_code = seeded.source_object_code
where not exists (
    select 1
    from payroll_engine.payroll_concept_operand existing
    where existing.target_object_id = target_object.id
      and existing.operand_role = seeded.operand_role
);

insert into payroll_engine.payroll_concept_feed_relation (
        source_object_id,
        target_object_id,
        feed_mode,
        feed_value,
        effective_from,
        effective_to
)
select
        source_object.id,
        target_object.id,
        seeded.feed_mode,
        seeded.feed_value,
        seeded.effective_from,
        seeded.effective_to
from (
        values
            ('ESP', 'CONSTANT', 'D01_FIXED_30', 'D01', 'FEED_BY_SOURCE', NUMERIC '30', DATE '2025-01-01', cast(null as date)),
                ('ESP', 'TABLE', 'P01_DAILY_AMOUNT_TABLE', 'P01', 'FEED_BY_SOURCE', cast(null as numeric), DATE '2025-01-01', cast(null as date))
) as seeded(rule_system_code, source_type_code, source_code, target_code, feed_mode, feed_value, effective_from, effective_to)
join payroll_engine.payroll_object source_object
    on source_object.rule_system_code = seeded.rule_system_code
 and source_object.object_type_code = seeded.source_type_code
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
    '101',
    true
where not exists (
    select 1
    from payroll.payroll_object_activation existing
    where existing.rule_system_code = 'ESP'
      and existing.owner_type_code = 'AGREEMENT'
            and existing.owner_code = '99002405011982'
      and existing.target_object_type_code = 'PAYROLL_CONCEPT'
      and existing.target_object_code = '101'
);

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
    'P01_DAILY_AMOUNT_TABLE',
    'TABLE',
    'P01_99002405011982',
    true
where not exists (
    select 1
    from payroll.payroll_object_binding existing
    where existing.rule_system_code = 'ESP'
      and existing.owner_type_code = 'AGREEMENT'
            and existing.owner_code = '99002405011982'
      and existing.binding_role_code = 'P01_DAILY_AMOUNT_TABLE'
);

insert into payroll.payroll_table_row (
    rule_system_code,
    table_code,
    search_code,
    start_date,
    end_date,
    daily_value,
    active
)
select
    seeded.rule_system_code,
    seeded.table_code,
    seeded.search_code,
    seeded.start_date,
    seeded.end_date,
    seeded.daily_value,
    seeded.active
from (
    values
    ('ESP', 'P01_99002405011982', '99002405-G1', DATE '2025-01-01', cast(null as date), NUMERIC '61.67', true),
    ('ESP', 'P01_99002405011982', '99002405-G2', DATE '2025-01-01', cast(null as date), NUMERIC '47.50', true),
    ('ESP', 'P01_99002405011982', '99002405-G3', DATE '2025-01-01', cast(null as date), NUMERIC '40.00', true)
) as seeded(rule_system_code, table_code, search_code, start_date, end_date, daily_value, active)
where not exists (
    select 1
    from payroll.payroll_table_row existing
    where existing.rule_system_code = seeded.rule_system_code
      and existing.table_code = seeded.table_code
      and existing.search_code = seeded.search_code
      and existing.start_date = seeded.start_date
);
