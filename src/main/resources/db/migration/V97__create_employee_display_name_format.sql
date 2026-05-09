CREATE TABLE rulesystem.employee_display_name_format (
    id                       BIGSERIAL    PRIMARY KEY,
    rule_system_code         VARCHAR(5)   NOT NULL UNIQUE,
    display_name_format_code VARCHAR(50)  NOT NULL,
    created_at               TIMESTAMP    NOT NULL,
    updated_at               TIMESTAMP    NOT NULL
);
