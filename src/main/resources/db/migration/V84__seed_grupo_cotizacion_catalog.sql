-- =========================================================
-- V84__seed_grupo_cotizacion_catalog.sql
-- Seed GRUPO_COTIZACION rule_entity_type and 11 standard ESP entries
-- =========================================================

insert into rulesystem.rule_entity_type (code, name)
values ('GRUPO_COTIZACION', 'Grupo de Cotización SS')
on conflict (code) do update
    set name = excluded.name;

insert into rulesystem.rule_entity (rule_system_code, rule_entity_type_code, code, name, description, active, start_date)
select
    rs.code,
    'GRUPO_COTIZACION',
    g.code,
    g.name,
    g.description,
    true,
    DATE '1900-01-01'
from rulesystem.rule_system rs
cross join (
    values
        ('01', 'Ingenieros y Licenciados', 'Personal de alta dirección no incluido en el art. 1.3.c) ET. Cotización mensual.'),
        ('02', 'Ingenieros Técnicos, Peritos y Ayudantes Titulados', 'Cotización mensual.'),
        ('03', 'Jefes Administrativos y de Taller', 'Cotización mensual.'),
        ('04', 'Ayudantes no Titulados', 'Cotización mensual.'),
        ('05', 'Oficiales Administrativos', 'Cotización mensual.'),
        ('06', 'Subalternos', 'Cotización mensual.'),
        ('07', 'Auxiliares Administrativos', 'Cotización mensual.'),
        ('08', 'Oficiales de primera y segunda', 'Cotización diaria.'),
        ('09', 'Oficiales de tercera y Especialistas', 'Cotización diaria.'),
        ('10', 'Peones', 'Cotización diaria.'),
        ('11', 'Trabajadores menores de dieciocho años, cualquier categoría', 'Cotización diaria.')
) as g(code, name, description)
on conflict (rule_system_code, rule_entity_type_code, code) do update
    set name = excluded.name,
        description = excluded.description,
        active = excluded.active;
