-- =========================================================
-- V87__seed_agreement_category_profile.sql
-- Link agreement categories from convenio 99002405011982
-- (Grandes Almacenes BOE-A-2023-13740) to their grupo de cotización SS
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
        ('99002405-G1', '01', 'MENSUAL'),
        ('99002405-G2', '05', 'MENSUAL'),
        ('99002405-G3', '07', 'MENSUAL')
) as mapping(category_code, grupo_cotizacion_code, tipo_nomina)
    on mapping.category_code = cat.code
where cat.rule_entity_type_code = 'AGREEMENT_CATEGORY'
on conflict (agreement_category_rule_entity_id) do update
    set grupo_cotizacion_code = excluded.grupo_cotizacion_code,
        tipo_nomina           = excluded.tipo_nomina,
        updated_at            = now();
