CREATE TABLE employee.employee_tax_information (
    id                      BIGSERIAL    PRIMARY KEY,
    employee_id             BIGINT       NOT NULL REFERENCES employee.employee(id),
    valid_from              DATE         NOT NULL,
    family_situation        VARCHAR(40)  NOT NULL
        CHECK (family_situation IN ('SINGLE_OR_OTHER','MARRIED_DEPENDENT_SPOUSE','SEPARATED_WITH_CHILDREN')),
    descendants_count       SMALLINT     NOT NULL DEFAULT 0 CHECK (descendants_count >= 0),
    ascendants_count        SMALLINT     NOT NULL DEFAULT 0 CHECK (ascendants_count >= 0),
    disability_degree       VARCHAR(20)  NOT NULL
        CHECK (disability_degree IN ('NONE','MODERATE','SEVERE')),
    pension_compensatoria   BOOLEAN      NOT NULL DEFAULT FALSE,
    geographic_mobility     BOOLEAN      NOT NULL DEFAULT FALSE,
    habitual_residence_loan BOOLEAN      NOT NULL DEFAULT FALSE,
    tax_territory           VARCHAR(20)  NOT NULL
        CHECK (tax_territory IN ('COMUN','ARABA','GIPUZKOA','BIZKAIA','NAVARRA')),
    created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_tax_info_employee_valid_from UNIQUE (employee_id, valid_from)
);

CREATE INDEX idx_tax_info_employee_id ON employee.employee_tax_information(employee_id);
