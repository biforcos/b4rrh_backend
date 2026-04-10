update authz.subject_role_assignment
set subject_code = 'bifor'
where subject_code = 'BIFOR'
  and role_code = 'ADMIN';

update authz.subject_role_assignment
set subject_code = 'hr.manager'
where subject_code = 'HR.MANAGER'
  and role_code = 'HR_MANAGER';

update authz.subject_role_assignment
set subject_code = 'hr.operator'
where subject_code = 'HR.OPERATOR'
  and role_code = 'HR_OPERATOR';

update authz.subject_role_assignment
set subject_code = 'auditor'
where subject_code = 'AUDITOR'
  and role_code = 'AUDITOR';

update authz.subject_role_assignment
set subject_code = 'readonly'
where subject_code = 'READONLY'
  and role_code = 'READONLY';