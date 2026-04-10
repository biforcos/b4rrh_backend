create table authz.subject_role_assignment (
    subject_code      varchar(100) not null,
    role_code         varchar(50)  not null,
    active            boolean      not null default true,
    assignment_origin varchar(20)  not null,
    created_at        timestamp    not null default now(),
    updated_at        timestamp    not null default now()
);

alter table authz.subject_role_assignment
    add constraint pk_subject_role_assignment
    primary key (subject_code, role_code);

alter table authz.subject_role_assignment
    add constraint fk_subject_role_assignment_role
    foreign key (role_code)
    references authz.role (code);

alter table authz.subject_role_assignment
    add constraint chk_subject_role_assignment_origin
    check (assignment_origin in ('INTERNAL', 'DEV', 'SYNC'));

create index idx_subject_role_assignment_subject_active
    on authz.subject_role_assignment (subject_code, active);

insert into authz.subject_role_assignment (subject_code, role_code, active, assignment_origin) values
    ('BIFOR', 'ADMIN', true, 'DEV'),
    ('HR.MANAGER', 'HR_MANAGER', true, 'DEV'),
    ('HR.OPERATOR', 'HR_OPERATOR', true, 'DEV'),
    ('AUDITOR', 'AUDITOR', true, 'DEV'),
    ('READONLY', 'READONLY', true, 'DEV');