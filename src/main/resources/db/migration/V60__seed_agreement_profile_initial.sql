-- =========================================================
-- V60__seed_agreement_profile_initial.sql
-- Seed initial agreement profiles with realistic annual hours
-- =========================================================

insert into rulesystem.agreement_profile (
    agreement_rule_entity_id,
    official_agreement_number,
    display_name,
    short_name,
    annual_hours,
    is_active,
    created_at,
    updated_at
)
select
    agr.id,
    prof.official_number,
    prof.display_name,
    prof.short_name,
    prof.annual_hours,
    true,
    now(),
    now()
from rulesystem.rule_entity agr
cross join (
    values
        ('AGR_OFFICE', 'CA-OFFICE-2024', 'Office Collective Agreement 2024', 'OFFICE', NUMERIC '1560.00'),
        ('AGR_TECH', 'CA-TECH-2024', 'Technical Collective Agreement 2024', 'TECH', NUMERIC '1560.00')
) as prof(code, official_number, display_name, short_name, annual_hours)
where agr.rule_entity_type_code = 'AGREEMENT'
  and agr.code = prof.code
on conflict (agreement_rule_entity_id) do update
set
    official_agreement_number = excluded.official_agreement_number,
    display_name = excluded.display_name,
    short_name = excluded.short_name,
    annual_hours = excluded.annual_hours,
    updated_at = now();
