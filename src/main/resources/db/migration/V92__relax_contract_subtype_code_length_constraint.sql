-- =========================================================
-- V92__relax_contract_subtype_code_length_constraint.sql
-- CONTRACT_SUBTYPE codes do not follow the same 3-char standard
-- as contract type codes (e.g. Spanish SEPE codes), so the
-- strict char_length = 3 check is replaced with a non-empty check.
-- =========================================================

alter table employee.contract
    drop constraint if exists chk_contract_subtype_code_length;

alter table employee.contract
    add constraint chk_contract_subtype_code_nonempty
    check (char_length(trim(contract_subtype_code)) > 0);
