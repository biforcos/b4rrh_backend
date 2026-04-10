alter table authz.role_resource_policy
    add column effect varchar(20);

update authz.role_resource_policy
set effect = 'ALLOW'
where effect is null;

alter table authz.role_resource_policy
    alter column effect set not null;

alter table authz.role_resource_policy
    alter column effect set default 'ALLOW';

alter table authz.role_resource_policy
    add constraint chk_rrp_effect
    check (effect in ('ALLOW', 'DENY'));