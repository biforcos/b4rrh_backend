INSERT INTO rulesystem.employee_numbering_config
    (rule_system_code, prefix, numeric_part_length, step, next_value, created_at, updated_at)
VALUES
    ('ESP', 'EMP', 6, 1, 1, NOW(), NOW());
-- Generates: EMP000001 … EMP999999 (1,000,000 employees)
