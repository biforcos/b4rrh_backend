alter table payroll_engine.payroll_concept
    drop constraint if exists fk_payroll_concept_object;

alter table payroll_engine.payroll_concept
    add constraint fk_payroll_concept_object
        foreign key (object_id)
        references payroll_engine.payroll_object(id)
        on delete cascade;
