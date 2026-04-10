-- =========================================================
-- V45__seed_authorization_initial_data.sql
-- Seed initial authorization data: families, roles, resources,
-- actions, permission profiles, profile-actions, and policies
-- =========================================================

-- resource families
insert into authz.resource_family (code, name, description) values
    ('EMPLOYEE_DATA',  'Employee Data',       'Core employee information: identity, contact, contract, presence, working time'),
    ('LIFECYCLE',      'Lifecycle',           'Employee lifecycle workflows: hire, terminate, rehire'),
    ('ORGANIZATION',   'Organization',        'Organizational structure: work center, cost center'),
    ('MASTER_DATA',    'Master Data',         'Rule system catalog and entity management'),
    ('ADMINISTRATION', 'Administration',      'Authorization model management');

-- roles
insert into authz.role (code, name, description) values
    ('ADMIN',            'Administrator',      'Full control over all resources'),
    ('HR_MANAGER',       'HR Manager',         'Manages all employee data and lifecycle workflows'),
    ('HR_OPERATOR',      'HR Operator',        'Manages employee contact, identifier, address and work center; can hire'),
    ('AUDITOR',          'Auditor',            'Read-only access to all resources'),
    ('CATALOG_MANAGER',  'Catalog Manager',    'Manages rule entities; read-only on rule systems and types'),
    ('READONLY',         'Read Only',          'Read-only access to all resources');

-- secured resources (root nodes first, then children)
insert into authz.secured_resource (code, parent_code, bounded_context_code, resource_kind, resource_family_code, name) values
    -- bounded contexts (roots)
    ('EMPLOYEE',                        null,              'employee',       'BOUNDED_CONTEXT', 'EMPLOYEE_DATA',  'Employee'),
    ('RULESYSTEM',                      null,              'rulesystem',     'BOUNDED_CONTEXT', 'MASTER_DATA',    'Rule System'),
    ('AUTHORIZATION',                   null,              'authorization',  'BOUNDED_CONTEXT', 'ADMINISTRATION', 'Authorization'),

    -- employee verticals
    ('EMPLOYEE.EMPLOYEE',               'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Core'),
    ('EMPLOYEE.CONTACT',                'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Contact'),
    ('EMPLOYEE.IDENTIFIER',             'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Identifier'),
    ('EMPLOYEE.ADDRESS',                'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Address'),
    ('EMPLOYEE.PRESENCE',               'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Presence'),
    ('EMPLOYEE.WORK_CENTER',            'EMPLOYEE',        'employee',       'VERTICAL',        'ORGANIZATION',   'Employee Work Center'),
    ('EMPLOYEE.WORKING_TIME',           'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Working Time'),
    ('EMPLOYEE.COST_CENTER',            'EMPLOYEE',        'employee',       'VERTICAL',        'ORGANIZATION',   'Employee Cost Center'),
    ('EMPLOYEE.CONTRACT',               'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Contract'),
    ('EMPLOYEE.LABOR_CLASSIFICATION',   'EMPLOYEE',        'employee',       'VERTICAL',        'EMPLOYEE_DATA',  'Employee Labor Classification'),

    -- lifecycle group and workflows
    ('EMPLOYEE.LIFECYCLE',              'EMPLOYEE',        'employee',       'GROUP',           'LIFECYCLE',      'Employee Lifecycle'),
    ('EMPLOYEE.LIFECYCLE.HIRE',         'EMPLOYEE.LIFECYCLE', 'employee',    'WORKFLOW',        'LIFECYCLE',      'Hire Employee'),
    ('EMPLOYEE.LIFECYCLE.TERMINATE',    'EMPLOYEE.LIFECYCLE', 'employee',    'WORKFLOW',        'LIFECYCLE',      'Terminate Employee'),
    ('EMPLOYEE.LIFECYCLE.REHIRE',       'EMPLOYEE.LIFECYCLE', 'employee',    'WORKFLOW',        'LIFECYCLE',      'Rehire Employee'),

    -- rulesystem verticals
    ('RULESYSTEM.RULE_SYSTEM',          'RULESYSTEM',      'rulesystem',     'VERTICAL',        'MASTER_DATA',    'Rule System'),
    ('RULESYSTEM.RULE_ENTITY_TYPE',     'RULESYSTEM',      'rulesystem',     'VERTICAL',        'MASTER_DATA',    'Rule Entity Type'),
    ('RULESYSTEM.RULE_ENTITY',          'RULESYSTEM',      'rulesystem',     'VERTICAL',        'MASTER_DATA',    'Rule Entity'),

    -- authorization admin resources
    ('AUTHORIZATION.ROLE',              'AUTHORIZATION',   'authorization',  'ADMIN_RESOURCE',  'ADMINISTRATION', 'Role'),
    ('AUTHORIZATION.SECURED_RESOURCE',  'AUTHORIZATION',   'authorization',  'ADMIN_RESOURCE',  'ADMINISTRATION', 'Secured Resource'),
    ('AUTHORIZATION.PERMISSION_PROFILE','AUTHORIZATION',   'authorization',  'ADMIN_RESOURCE',  'ADMINISTRATION', 'Permission Profile'),
    ('AUTHORIZATION.POLICY',            'AUTHORIZATION',   'authorization',  'ADMIN_RESOURCE',  'ADMINISTRATION', 'Role Resource Policy');

-- actions
insert into authz.action (code, name) values
    ('READ',    'Read'),
    ('CREATE',  'Create'),
    ('UPDATE',  'Update'),
    ('DELETE',  'Delete'),
    ('CLOSE',   'Close'),
    ('CORRECT', 'Correct'),
    ('EXECUTE', 'Execute'),
    ('ADMIN',   'Admin');

-- permission profiles
insert into authz.permission_profile (code, name, description) values
    ('NONE',               'None',               'No actions granted'),
    ('READ_ONLY',          'Read Only',          'Read access only'),
    ('SLOT_MAINTAINER',    'Slot Maintainer',    'Full CRUD on slot-based resources (contact, identifier)'),
    ('TEMPORAL_MAINTAINER','Temporal Maintainer','Full CRUD plus correction on temporal resources (address, work center, working time)'),
    ('WORKFLOW_EXECUTOR',  'Workflow Executor',  'Read and execute workflows'),
    ('FULL_CONTROL',       'Full Control',       'All actions granted');

-- permission profile actions
insert into authz.permission_profile_action (permission_profile_code, action_code) values
    -- NONE: (no rows — no actions)

    -- READ_ONLY
    ('READ_ONLY', 'READ'),

    -- SLOT_MAINTAINER
    ('SLOT_MAINTAINER', 'READ'),
    ('SLOT_MAINTAINER', 'CREATE'),
    ('SLOT_MAINTAINER', 'UPDATE'),
    ('SLOT_MAINTAINER', 'DELETE'),

    -- TEMPORAL_MAINTAINER
    ('TEMPORAL_MAINTAINER', 'READ'),
    ('TEMPORAL_MAINTAINER', 'CREATE'),
    ('TEMPORAL_MAINTAINER', 'UPDATE'),
    ('TEMPORAL_MAINTAINER', 'DELETE'),
    ('TEMPORAL_MAINTAINER', 'CORRECT'),

    -- WORKFLOW_EXECUTOR
    ('WORKFLOW_EXECUTOR', 'READ'),
    ('WORKFLOW_EXECUTOR', 'EXECUTE'),

    -- FULL_CONTROL
    ('FULL_CONTROL', 'READ'),
    ('FULL_CONTROL', 'CREATE'),
    ('FULL_CONTROL', 'UPDATE'),
    ('FULL_CONTROL', 'DELETE'),
    ('FULL_CONTROL', 'CLOSE'),
    ('FULL_CONTROL', 'CORRECT'),
    ('FULL_CONTROL', 'EXECUTE'),
    ('FULL_CONTROL', 'ADMIN');

-- role resource policies
insert into authz.role_resource_policy (role_code, resource_code, permission_profile_code, propagation_mode) values

    -- AUDITOR: read-only on everything
    ('AUDITOR', 'EMPLOYEE',    'READ_ONLY', 'THIS_RESOURCE_AND_CHILDREN'),
    ('AUDITOR', 'RULESYSTEM',  'READ_ONLY', 'THIS_RESOURCE_AND_CHILDREN'),

    -- HR_OPERATOR: read employee tree, maintain contact/identifier/address/work_center/working_time, hire only
    ('HR_OPERATOR', 'EMPLOYEE',                   'READ_ONLY',          'THIS_RESOURCE_AND_CHILDREN'),
    ('HR_OPERATOR', 'EMPLOYEE.CONTACT',            'SLOT_MAINTAINER',    'THIS_RESOURCE_ONLY'),
    ('HR_OPERATOR', 'EMPLOYEE.IDENTIFIER',         'SLOT_MAINTAINER',    'THIS_RESOURCE_ONLY'),
    ('HR_OPERATOR', 'EMPLOYEE.ADDRESS',            'TEMPORAL_MAINTAINER','THIS_RESOURCE_ONLY'),
    ('HR_OPERATOR', 'EMPLOYEE.WORK_CENTER',        'TEMPORAL_MAINTAINER','THIS_RESOURCE_ONLY'),
    ('HR_OPERATOR', 'EMPLOYEE.WORKING_TIME',       'TEMPORAL_MAINTAINER','THIS_RESOURCE_ONLY'),
    ('HR_OPERATOR', 'EMPLOYEE.LIFECYCLE.HIRE',     'WORKFLOW_EXECUTOR',  'THIS_RESOURCE_ONLY'),
    ('HR_OPERATOR', 'EMPLOYEE.LIFECYCLE.TERMINATE','NONE',               'THIS_RESOURCE_ONLY'),
    ('HR_OPERATOR', 'EMPLOYEE.LIFECYCLE.REHIRE',   'NONE',               'THIS_RESOURCE_ONLY'),

    -- HR_MANAGER: read employee tree, maintain contact/identifier/address/work_center/working_time, all lifecycle workflows
    ('HR_MANAGER', 'EMPLOYEE',                 'READ_ONLY',          'THIS_RESOURCE_AND_CHILDREN'),
    ('HR_MANAGER', 'EMPLOYEE.CONTACT',          'SLOT_MAINTAINER',    'THIS_RESOURCE_ONLY'),
    ('HR_MANAGER', 'EMPLOYEE.IDENTIFIER',       'SLOT_MAINTAINER',    'THIS_RESOURCE_ONLY'),
    ('HR_MANAGER', 'EMPLOYEE.ADDRESS',          'TEMPORAL_MAINTAINER','THIS_RESOURCE_ONLY'),
    ('HR_MANAGER', 'EMPLOYEE.WORK_CENTER',      'TEMPORAL_MAINTAINER','THIS_RESOURCE_ONLY'),
    ('HR_MANAGER', 'EMPLOYEE.WORKING_TIME',     'TEMPORAL_MAINTAINER','THIS_RESOURCE_ONLY'),
    ('HR_MANAGER', 'EMPLOYEE.LIFECYCLE',        'WORKFLOW_EXECUTOR',  'THIS_RESOURCE_AND_CHILDREN'),

    -- CATALOG_MANAGER: manage rule entities, read-only on system and types
    ('CATALOG_MANAGER', 'RULESYSTEM.RULE_ENTITY',      'FULL_CONTROL', 'THIS_RESOURCE_ONLY'),
    ('CATALOG_MANAGER', 'RULESYSTEM.RULE_ENTITY_TYPE', 'READ_ONLY',    'THIS_RESOURCE_ONLY'),
    ('CATALOG_MANAGER', 'RULESYSTEM.RULE_SYSTEM',      'READ_ONLY',    'THIS_RESOURCE_ONLY'),

    -- ADMIN: full control over everything
    ('ADMIN', 'EMPLOYEE',       'FULL_CONTROL', 'THIS_RESOURCE_AND_CHILDREN'),
    ('ADMIN', 'RULESYSTEM',     'FULL_CONTROL', 'THIS_RESOURCE_AND_CHILDREN'),
    ('ADMIN', 'AUTHORIZATION',  'FULL_CONTROL', 'THIS_RESOURCE_AND_CHILDREN'),

    -- READONLY: read-only on everything
    ('READONLY', 'EMPLOYEE',   'READ_ONLY', 'THIS_RESOURCE_AND_CHILDREN'),
    ('READONLY', 'RULESYSTEM', 'READ_ONLY', 'THIS_RESOURCE_AND_CHILDREN');
