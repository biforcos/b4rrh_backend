do $$
begin
    if exists (
        select 1
        from pg_constraint
        where conname = 'fk_working_time_employee'
          and conrelid = 'employee.working_time'::regclass
          and pg_get_constraintdef(oid) not ilike '%ON DELETE CASCADE%'
    ) then
        alter table employee.working_time
            drop constraint fk_working_time_employee;

        alter table employee.working_time
            add constraint fk_working_time_employee
            foreign key (employee_id)
            references employee.employee(id)
            on delete cascade;
    end if;
end
$$;