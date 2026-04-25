-- =========================================================
-- V73: seed concept_assignment for 101, 970, 990
--
-- Enables eligibility-based execution for agreement 99002405011982.
-- Concept 980 (TOTAL_DEDUCTION) is intentionally excluded because it
-- has no active feed sources, which would cause MissingAggregateSourcesException.
-- =========================================================
insert into payroll_engine.concept_assignment
    (rule_system_code, concept_code, company_code, agreement_code, employee_type_code,
     valid_from, valid_to, priority, created_at, updated_at)
values
    ('ESP', '101', null, '99002405011982', null, '2025-01-01', null, 10,  CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ESP', '970', null, '99002405011982', null, '2025-01-01', null, 970, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('ESP', '990', null, '99002405011982', null, '2025-01-01', null, 990, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
