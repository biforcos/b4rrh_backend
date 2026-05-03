-- =========================================================
-- V87__seed_agreement_category_profile.sql
-- Link ESP seed agreement categories to their grupo de cotización
-- =========================================================

insert into rulesystem.agreement_category_profile (
    agreement_category_rule_entity_id,
    grupo_cotizacion_code,
    tipo_nomina
)
select
    cat.id,
    mapping.grupo_cotizacion_code,
    mapping.tipo_nomina
from rulesystem.rule_entity cat
join (
    values
        ('CAT_ADMIN',   '05', 'MENSUAL'),
        ('CAT_TECH_1',  '01', 'MENSUAL'),
        ('CAT_TECH_2',  '02', 'MENSUAL')
) as mapping(category_code, grupo_cotizacion_code, tipo_nomina)
    on mapping.category_code = cat.code
where cat.rule_entity_type_code = 'AGREEMENT_CATEGORY'
on conflict (agreement_category_rule_entity_id) do update
    set grupo_cotizacion_code = excluded.grupo_cotizacion_code,
        tipo_nomina           = excluded.tipo_nomina,
        updated_at            = now();
