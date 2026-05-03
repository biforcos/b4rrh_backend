-- =========================================================
-- V89__rename_java_provided_to_engine_provided.sql
--
-- Renames calculation_type 'JAVA_PROVIDED' → 'ENGINE_PROVIDED' in all
-- existing payroll_concept rows. Reflects the CalculationType enum rename.
-- =========================================================

update payroll_engine.payroll_concept
set calculation_type = 'ENGINE_PROVIDED',
    updated_at       = current_timestamp
where calculation_type = 'JAVA_PROVIDED';
