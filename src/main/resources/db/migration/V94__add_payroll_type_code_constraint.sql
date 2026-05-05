-- =========================================================
-- V94__add_payroll_type_code_constraint.sql
-- Clean up legacy 'MENSUAL' rows and enforce CHECK constraint
-- for payroll_type_code on all payroll tables.
-- Valid values: 'NORMAL', 'EXTRA'
-- =========================================================

-- ---------------------------------------------------------
-- 1. Cleanup: replace legacy 'MENSUAL' with 'NORMAL'
-- ---------------------------------------------------------

UPDATE payroll.payroll
SET payroll_type_code = 'NORMAL'
WHERE payroll_type_code = 'MENSUAL';

UPDATE payroll.calculation_run
SET payroll_type_code = 'NORMAL'
WHERE payroll_type_code = 'MENSUAL';

UPDATE payroll.calculation_claim
SET payroll_type_code = 'NORMAL'
WHERE payroll_type_code = 'MENSUAL';

UPDATE payroll.calculation_run_message
SET payroll_type_code = 'NORMAL'
WHERE payroll_type_code = 'MENSUAL';

-- ---------------------------------------------------------
-- 2. Add CHECK constraints
-- ---------------------------------------------------------

ALTER TABLE payroll.payroll
    ADD CONSTRAINT payroll_payroll_type_code_check
        CHECK (payroll_type_code IN ('NORMAL', 'EXTRA'));

ALTER TABLE payroll.calculation_run
    ADD CONSTRAINT calculation_run_payroll_type_code_check
        CHECK (payroll_type_code IN ('NORMAL', 'EXTRA'));

ALTER TABLE payroll.calculation_claim
    ADD CONSTRAINT calculation_claim_payroll_type_code_check
        CHECK (payroll_type_code IN ('NORMAL', 'EXTRA'));

ALTER TABLE payroll.calculation_run_message
    ADD CONSTRAINT calculation_run_message_payroll_type_code_check
        CHECK (payroll_type_code IS NULL OR payroll_type_code IN ('NORMAL', 'EXTRA'));
