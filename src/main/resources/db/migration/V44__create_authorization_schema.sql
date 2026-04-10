-- =========================================================
-- V44__create_authorization_schema.sql
-- Create authz schema and all tables
-- (schema named 'authz' to avoid collision with the SQL
--  reserved keyword AUTHORIZATION)
-- =========================================================

create schema authz;

-- resource_family: semantic grouping of resources (not hierarchy)
create table authz.resource_family (
    code        varchar(50)  not null,
    name        varchar(100) not null,
    description text,
    active      boolean      not null default true,
    created_at  timestamp    not null default now(),
    updated_at  timestamp    not null default now()
);

alter table authz.resource_family
    add constraint pk_resource_family primary key (code);

-- role: functional roles
create table authz.role (
    code        varchar(50)  not null,
    name        varchar(100) not null,
    description text,
    active      boolean      not null default true,
    created_at  timestamp    not null default now(),
    updated_at  timestamp    not null default now()
);

alter table authz.role
    add constraint pk_role primary key (code);

-- secured_resource: hierarchical catalog of protected resources
create table authz.secured_resource (
    code                 varchar(100) not null,
    parent_code          varchar(100),
    bounded_context_code varchar(50)  not null,
    resource_kind        varchar(50)  not null,
    resource_family_code varchar(50)  not null,
    name                 varchar(100) not null,
    description          text,
    active               boolean      not null default true,
    created_at           timestamp    not null default now(),
    updated_at           timestamp    not null default now()
);

alter table authz.secured_resource
    add constraint pk_secured_resource primary key (code);

alter table authz.secured_resource
    add constraint fk_secured_resource_parent
    foreign key (parent_code)
    references authz.secured_resource (code);

alter table authz.secured_resource
    add constraint fk_secured_resource_family
    foreign key (resource_family_code)
    references authz.resource_family (code);

alter table authz.secured_resource
    add constraint chk_secured_resource_kind
    check (resource_kind in ('BOUNDED_CONTEXT', 'VERTICAL', 'WORKFLOW', 'GROUP', 'ADMIN_RESOURCE'));

-- action: semantic action catalog
create table authz.action (
    code        varchar(50)  not null,
    name        varchar(100) not null,
    description text,
    active      boolean      not null default true,
    created_at  timestamp    not null default now(),
    updated_at  timestamp    not null default now()
);

alter table authz.action
    add constraint pk_action primary key (code);

-- permission_profile: reusable permission sets
create table authz.permission_profile (
    code        varchar(50)  not null,
    name        varchar(100) not null,
    description text,
    active      boolean      not null default true,
    created_at  timestamp    not null default now(),
    updated_at  timestamp    not null default now()
);

alter table authz.permission_profile
    add constraint pk_permission_profile primary key (code);

-- permission_profile_action: profile-to-action composition
create table authz.permission_profile_action (
    permission_profile_code varchar(50) not null,
    action_code             varchar(50) not null,
    created_at              timestamp   not null default now()
);

alter table authz.permission_profile_action
    add constraint pk_permission_profile_action
    primary key (permission_profile_code, action_code);

alter table authz.permission_profile_action
    add constraint fk_ppa_permission_profile
    foreign key (permission_profile_code)
    references authz.permission_profile (code);

alter table authz.permission_profile_action
    add constraint fk_ppa_action
    foreign key (action_code)
    references authz.action (code);

-- role_resource_policy: central table (role → resource → profile + propagation)
create table authz.role_resource_policy (
    role_code               varchar(50)  not null,
    resource_code           varchar(100) not null,
    permission_profile_code varchar(50)  not null,
    propagation_mode        varchar(50)  not null,
    active                  boolean      not null default true,
    created_at              timestamp    not null default now(),
    updated_at              timestamp    not null default now()
);

alter table authz.role_resource_policy
    add constraint pk_role_resource_policy
    primary key (role_code, resource_code);

alter table authz.role_resource_policy
    add constraint fk_rrp_role
    foreign key (role_code)
    references authz.role (code);

alter table authz.role_resource_policy
    add constraint fk_rrp_secured_resource
    foreign key (resource_code)
    references authz.secured_resource (code);

alter table authz.role_resource_policy
    add constraint fk_rrp_permission_profile
    foreign key (permission_profile_code)
    references authz.permission_profile (code);

alter table authz.role_resource_policy
    add constraint chk_rrp_propagation_mode
    check (propagation_mode in ('THIS_RESOURCE_ONLY', 'THIS_RESOURCE_AND_CHILDREN'));

create index idx_rrp_role_resource
    on authz.role_resource_policy (role_code, resource_code);
