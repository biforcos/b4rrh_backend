CREATE TABLE employee.employee_payroll_input (
    id                 BIGSERIAL       NOT NULL,
    rule_system_code   VARCHAR(10)     NOT NULL,
    employee_type_code VARCHAR(10)     NOT NULL,
    employee_number    VARCHAR(20)     NOT NULL,
    concept_code       VARCHAR(20)     NOT NULL,
    period             INTEGER         NOT NULL,
    quantity           NUMERIC(14,4)   NOT NULL,
    CONSTRAINT pk_employee_payroll_input PRIMARY KEY (id),
    CONSTRAINT uq_employee_payroll_input_bk
        UNIQUE (rule_system_code, employee_type_code, employee_number, concept_code, period)
);
