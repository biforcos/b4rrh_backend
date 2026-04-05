do $$
declare
    employee_id_attnum smallint;
    fk record;
    base_definition text;
begin
    select attnum
    into employee_id_attnum
    from pg_attribute
    where attrelid = 'employee.employee'::regclass
      and attname = 'id'
      and not attisdropped;

    for fk in
        select
            con.conname,
            nsp.nspname as schema_name,
            rel.relname as table_name,
            pg_get_constraintdef(con.oid) as definition
        from pg_constraint con
        join pg_class rel on rel.oid = con.conrelid
        join pg_namespace nsp on nsp.oid = rel.relnamespace
        where con.contype = 'f'
          and nsp.nspname = 'employee'
          and con.confrelid = 'employee.employee'::regclass
          and con.confkey = ARRAY[employee_id_attnum]::smallint[]
          and pg_get_constraintdef(con.oid) not ilike '%ON DELETE CASCADE%'
    loop
            base_definition := regexp_replace(
                fk.definition,
                '\s+ON\s+DELETE\s+\w+',
                '',
                'i'
            );

            execute format(
                'alter table %I.%I drop constraint %I',
                fk.schema_name,
                fk.table_name,
                fk.conname
            );

            execute format(
                'alter table %I.%I add constraint %I %s ON DELETE CASCADE',
                fk.schema_name,
                fk.table_name,
                fk.conname,
                base_definition
            );
    end loop;
end
$$;
