-- =========================================================
-- V30__add_contract_code_length_checks.sql
-- Enforce exact length for employee.contract functional codes
-- =========================================================

alter table employee.contract
    drop constraint if exists chk_contract_code_length;

alter table employee.contract
    add constraint chk_contract_code_length
    check (char_length(contract_code) = 3);

alter table employee.contract
    drop constraint if exists chk_contract_subtype_code_length;

alter table employee.contract
    add constraint chk_contract_subtype_code_length
    check (char_length(contract_subtype_code) = 3);
