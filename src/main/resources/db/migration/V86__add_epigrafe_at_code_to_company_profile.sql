-- =========================================================
-- V86__add_epigrafe_at_code_to_company_profile.sql
-- Add nullable epigrafe AT/EP code to rulesystem.company_profile
-- =========================================================

alter table rulesystem.company_profile
    add column epigrafe_at_code varchar(10);
