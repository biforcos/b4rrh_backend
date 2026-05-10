CREATE TABLE rulesystem.employee_numbering_config (
    id                  BIGSERIAL    NOT NULL,
    rule_system_code    VARCHAR(20)  NOT NULL,
    prefix              VARCHAR(14)  NOT NULL DEFAULT '',
    numeric_part_length INT          NOT NULL,
    step                INT          NOT NULL DEFAULT 1,
    next_value          BIGINT       NOT NULL DEFAULT 1,
    created_at          TIMESTAMP    NOT NULL,
    updated_at          TIMESTAMP    NOT NULL,
    CONSTRAINT pk_employee_numbering_config PRIMARY KEY (id),
    CONSTRAINT uk_employee_numbering_config_rs UNIQUE (rule_system_code),
    CONSTRAINT fk_employee_numbering_config_rs
        FOREIGN KEY (rule_system_code) REFERENCES rulesystem.rule_system(code),
    CONSTRAINT chk_employee_numbering_config_length
        CHECK (LENGTH(prefix) + numeric_part_length <= 15),
    CONSTRAINT chk_employee_numbering_config_part_min
        CHECK (numeric_part_length >= 1),
    CONSTRAINT chk_employee_numbering_config_step_min
        CHECK (step >= 1),
    CONSTRAINT chk_employee_numbering_config_next_min
        CHECK (next_value >= 1)
);
