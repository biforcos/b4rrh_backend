alter table rulesystem.work_center_profile
    alter column company_code set not null;

create index if not exists idx_work_center_profile_company_code
    on rulesystem.work_center_profile (company_code);
