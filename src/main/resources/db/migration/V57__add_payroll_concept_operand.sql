create table payroll_engine.payroll_concept_operand (
    id               bigint generated always as identity primary key,
    target_object_id bigint      not null,
    operand_role     varchar(30) not null,
    source_object_id bigint      not null,
    created_at       timestamp   not null default now(),
    updated_at       timestamp   not null default now(),
    constraint fk_concept_operand_target
        foreign key (target_object_id)
        references payroll_engine.payroll_object(id),
    constraint fk_concept_operand_source
        foreign key (source_object_id)
        references payroll_engine.payroll_object(id),
    constraint uk_concept_operand_role
        unique (target_object_id, operand_role)
);
