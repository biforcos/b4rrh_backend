package com.b4rrhh.employee.lifecycle.application.usecase;

import com.b4rrhh.B4rrhhBackendApplication;
import com.b4rrhh.employee.lifecycle.application.command.HireEmployeeCommand;
import com.b4rrhh.employee.lifecycle.application.model.HireEmployeeResult;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = B4rrhhBackendApplication.class,
        properties = {
                "spring.datasource.url=jdbc:h2:mem:baseline-hire;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;DEFAULT_NULL_ORDERING=HIGH",
                "spring.datasource.driver-class-name=org.h2.Driver",
                "spring.datasource.username=sa",
                "spring.datasource.password=",
                "spring.jpa.hibernate.ddl-auto=none",
                "spring.flyway.enabled=true"
        }
)
class HireEmployeeBaselineFlywayIntegrationTest {

    @TempDir
    static Path tempDir;

    @Autowired
    private HireEmployeeUseCase hireEmployeeUseCase;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) throws IOException {
        Path migrationDirectory = Files.createDirectories(tempDir.resolve("flyway-hire-baseline"));

        writeFoundationMigration(migrationDirectory.resolve("V1__baseline_hire_foundation.sql"));
        copyMigration(migrationDirectory, "V49__seed_esp_baseline_rule_system.sql");
        copyMigration(migrationDirectory, "V50__seed_esp_baseline_catalogs.sql");
        copyMigration(migrationDirectory, "V51__seed_esp_baseline_organization.sql");
        copyMigration(migrationDirectory, "V52__seed_esp_baseline_relationships.sql");

        registry.add("spring.flyway.locations", () -> "filesystem:" + migrationDirectory.toAbsolutePath());
    writeAgreementProfilesMigration(migrationDirectory.resolve("V53__baseline_agreement_profiles.sql"));
    }

        @ParameterizedTest(name = "{0}")
        @MethodSource("baselineHireScenarios")
        void hiresEmployeeSuccessfullyForAllCoherentBaselineScenarios(HireScenario scenario) {
        HireEmployeeResult result = hireEmployeeUseCase.hire(new HireEmployeeCommand(
            "ESP",
            "INTERNAL",
            scenario.firstName(),
            scenario.lastName1(),
            null,
            scenario.preferredName(),
            scenario.hireDate(),
            "HIRING",
            scenario.companyCode(),
            scenario.workCenterCode(),
            new HireEmployeeCommand.HireEmployeeContractCommand(scenario.contractCode(), scenario.contractSubtypeCode()),
            new HireEmployeeCommand.HireEmployeeLaborClassificationCommand(
                scenario.agreementCode(),
                scenario.agreementCategoryCode()
            ),
            null,
            new HireEmployeeCommand.HireEmployeeWorkingTimeCommand(scenario.workingTimePercentage())
        ));

        String generatedNumber = result.employee().employeeNumber();

        assertThat(result.employee().ruleSystemCode()).isEqualTo("ESP");
        assertThat(result.employee().employeeTypeCode()).isEqualTo("INTERNAL");
        assertThat(generatedNumber).isNotNull().isNotBlank();
        assertThat(result.presence().companyCode()).isEqualTo(scenario.companyCode());
        assertThat(result.workCenter().workCenterCode()).isEqualTo(scenario.workCenterCode());
        assertThat(result.contract().contractTypeCode()).isEqualTo(scenario.contractCode());
        assertThat(result.contract().contractSubtypeCode()).isEqualTo(scenario.contractSubtypeCode());
        assertThat(result.laborClassification().agreementCode()).isEqualTo(scenario.agreementCode());
        assertThat(result.laborClassification().agreementCategoryCode()).isEqualTo(scenario.agreementCategoryCode());

        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?",
            Integer.class,
            "ESP",
            "INTERNAL",
            generatedNumber
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from employee.presence where company_code = ? and employee_id = (select id from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?)",
            Integer.class,
            scenario.companyCode(),
            "ESP",
            "INTERNAL",
            generatedNumber
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from employee.labor_classification where agreement_code = ? and agreement_category_code = ? and employee_id = (select id from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?)",
            Integer.class,
            scenario.agreementCode(),
            scenario.agreementCategoryCode(),
            "ESP",
            "INTERNAL",
            generatedNumber
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from employee.contract where contract_code = ? and contract_subtype_code = ? and employee_id = (select id from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?)",
            Integer.class,
            scenario.contractCode(),
            scenario.contractSubtypeCode(),
            "ESP",
            "INTERNAL",
            generatedNumber
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from employee.work_center where work_center_code = ? and employee_id = (select id from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?)",
            Integer.class,
            scenario.workCenterCode(),
            "ESP",
            "INTERNAL",
            generatedNumber
        )).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
            "select count(*) from employee.working_time where employee_id = (select id from employee.employee where rule_system_code = ? and employee_type_code = ? and employee_number = ?)",
            Integer.class,
            "ESP",
            "INTERNAL",
            generatedNumber
        )).isEqualTo(1);
    }

        private static Stream<HireScenario> baselineHireScenarios() {
        return Stream.of(
            new HireScenario(
                "Scenario Base - Office Indefinite",
                "Ana",
                "Lopez",
                "Ani",
                LocalDate.of(2026, 4, 1),
                "ES01",
                "MAIN_OFFICE",
                "AGR_OFFICE",
                "CAT_ADMIN",
                "IND",
                "FT1",
                new BigDecimal("100")
            ),
            new HireScenario(
                "Scenario A - Technical Employee",
                "Bruno",
                "Martin",
                "Bru",
                LocalDate.of(2026, 4, 2),
                "ES01",
                "BRANCH_NORTH",
                "AGR_TECH",
                "CAT_TECH_1",
                "IND",
                "FT1",
                new BigDecimal("100")
            ),
            new HireScenario(
                "Scenario B - Temporary Part Time",
                "Carla",
                "Diaz",
                "Car",
                LocalDate.of(2026, 4, 3),
                "ES01",
                "MAIN_OFFICE",
                "AGR_OFFICE",
                "CAT_ADMIN",
                "TMP",
                "PT1",
                new BigDecimal("60")
            ),
            new HireScenario(
                "Scenario C - Second Company",
                "Dario",
                "Santos",
                "Dari",
                LocalDate.of(2026, 4, 4),
                "ES02",
                "BRANCH_SOUTH",
                "AGR_TECH",
                "CAT_TECH_2",
                "IND",
                "FT1",
                new BigDecimal("100")
            )
        );
        }

        private record HireScenario(
            String name,
            String firstName,
            String lastName1,
            String preferredName,
            LocalDate hireDate,
            String companyCode,
            String workCenterCode,
            String agreementCode,
            String agreementCategoryCode,
            String contractCode,
            String contractSubtypeCode,
            BigDecimal workingTimePercentage
        ) {
        @Override
        public String toString() {
            return name;
        }
        }

    private static void writeAgreementProfilesMigration(Path filePath) throws IOException {
        Files.writeString(filePath, """
                insert into rulesystem.agreement_profile (
                    agreement_rule_entity_id,
                    official_agreement_number,
                    display_name,
                    annual_hours,
                    is_active
                )
                select
                    re.id,
                    re.code,
                    re.name,
                    1736.00,
                    true
                from rulesystem.rule_entity re
                where re.rule_entity_type_code = 'AGREEMENT'
                  and re.code in ('AGR_OFFICE', 'AGR_TECH');
                """, StandardCharsets.UTF_8);
    }

    private static void copyMigration(Path migrationDirectory, String fileName) throws IOException {
        Path source = Path.of(
                "src",
                "main",
                "resources",
                "db",
                "migration",
                fileName
        );
        Files.copy(source, migrationDirectory.resolve(fileName));
    }

    private static void writeFoundationMigration(Path filePath) throws IOException {
        Files.writeString(filePath, """
                create schema if not exists rulesystem;
                create schema if not exists employee;

                create table rulesystem.rule_system (
                    id bigint generated always as identity primary key,
                    code varchar(5) not null,
                    name varchar(100) not null,
                    country_code char(3) not null,
                    active boolean not null default true,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint uk_rule_system_code unique (code)
                );

                create table rulesystem.rule_entity_type (
                    id bigint generated always as identity primary key,
                    code varchar(30) not null,
                    name varchar(100) not null,
                    active boolean not null default true,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint uk_rule_entity_type_code unique (code)
                );

                create table rulesystem.rule_entity (
                    id bigint generated always as identity primary key,
                    rule_system_code varchar(5) not null,
                    rule_entity_type_code varchar(30) not null,
                    code varchar(30) not null,
                    name varchar(100) not null,
                    description varchar(500),
                    active boolean not null default true,
                    start_date date not null,
                    end_date date,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_rule_entity_rule_system_code foreign key (rule_system_code) references rulesystem.rule_system(code),
                    constraint fk_rule_entity_type_code foreign key (rule_entity_type_code) references rulesystem.rule_entity_type(code),
                    constraint uk_rule_entity_business unique (rule_system_code, rule_entity_type_code, code),
                    constraint chk_rule_entity_dates check (end_date is null or start_date <= end_date)
                );

                    create table rulesystem.agreement_profile (
                        id bigint generated always as identity primary key,
                        agreement_rule_entity_id bigint not null,
                        official_agreement_number varchar(50) not null,
                        display_name varchar(200) not null,
                        short_name varchar(50),
                        annual_hours numeric(7, 2) not null,
                        is_active boolean not null default true,
                        created_at timestamp not null default now(),
                        updated_at timestamp not null default now(),
                        constraint uk_agreement_profile_rule_entity unique (agreement_rule_entity_id),
                        constraint fk_agreement_profile_rule_entity foreign key (agreement_rule_entity_id) references rulesystem.rule_entity(id)
                    );

                create table rulesystem.agreement_category_relation (
                    id bigint generated always as identity primary key,
                    rule_system_id bigint not null,
                    agreement_rule_entity_id bigint not null,
                    category_rule_entity_id bigint not null,
                    start_date date not null,
                    end_date date,
                    is_active boolean not null default true,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_agreement_category_relation_rule_system foreign key (rule_system_id) references rulesystem.rule_system(id),
                    constraint fk_agreement_category_relation_agreement foreign key (agreement_rule_entity_id) references rulesystem.rule_entity(id),
                    constraint fk_agreement_category_relation_category foreign key (category_rule_entity_id) references rulesystem.rule_entity(id),
                    constraint chk_agreement_category_relation_dates check (end_date is null or start_date <= end_date)
                );

                create table rulesystem.contract_subtype_relation (
                    id bigint generated always as identity primary key,
                    rule_system_id bigint not null,
                    contract_rule_entity_id bigint not null,
                    subtype_rule_entity_id bigint not null,
                    start_date date not null,
                    end_date date,
                    is_active boolean not null default true,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_contract_subtype_relation_rule_system foreign key (rule_system_id) references rulesystem.rule_system(id),
                    constraint fk_contract_subtype_relation_contract foreign key (contract_rule_entity_id) references rulesystem.rule_entity(id),
                    constraint fk_contract_subtype_relation_subtype foreign key (subtype_rule_entity_id) references rulesystem.rule_entity(id),
                    constraint chk_contract_subtype_relation_dates check (end_date is null or start_date <= end_date)
                );

                create table rulesystem.company_profile (
                    id bigint generated always as identity primary key,
                    company_rule_entity_id bigint not null,
                    legal_name varchar(200) not null,
                    tax_identifier varchar(50),
                    street varchar(300),
                    city varchar(120),
                    postal_code varchar(20),
                    region_code varchar(30),
                    country_code char(3),
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint uk_company_profile_company_rule_entity unique (company_rule_entity_id),
                    constraint fk_company_profile_company_rule_entity foreign key (company_rule_entity_id) references rulesystem.rule_entity(id)
                );

                create table rulesystem.work_center_profile (
                    id bigint generated always as identity primary key,
                    work_center_rule_entity_id bigint not null,
                    company_code varchar(30) not null,
                    street varchar(300),
                    city varchar(120),
                    postal_code varchar(20),
                    region_code varchar(30),
                    country_code char(3),
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint uk_work_center_profile_rule_entity unique (work_center_rule_entity_id),
                    constraint fk_work_center_profile_rule_entity foreign key (work_center_rule_entity_id) references rulesystem.rule_entity(id)
                );

                create table employee.employee (
                    id bigint generated always as identity primary key,
                    rule_system_code varchar(5) not null,
                    employee_type_code varchar(30) not null,
                    employee_number varchar(15) not null,
                    first_name varchar(100) not null,
                    last_name_1 varchar(100) not null,
                    last_name_2 varchar(100),
                    preferred_name varchar(300),
                    status varchar(30) not null default 'ACTIVE',
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    photo_url varchar(512),
                    constraint fk_employee_rule_system_code foreign key (rule_system_code) references rulesystem.rule_system(code),
                    constraint uk_employee_business_key unique (rule_system_code, employee_type_code, employee_number)
                );

                create table employee.presence (
                    id bigint generated always as identity primary key,
                    employee_id bigint not null,
                    presence_number integer not null,
                    company_code varchar(30) not null,
                    entry_reason_code varchar(30) not null,
                    exit_reason_code varchar(30),
                    start_date date not null,
                    end_date date,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_presence_employee foreign key (employee_id) references employee.employee(id),
                    constraint uk_presence_number unique (employee_id, presence_number),
                    constraint uk_presence_start_date unique (employee_id, start_date),
                    constraint chk_presence_dates check (end_date is null or start_date <= end_date)
                );

                create table employee.labor_classification (
                    id bigint generated always as identity primary key,
                    employee_id bigint not null,
                    agreement_code varchar(30) not null,
                    agreement_category_code varchar(30) not null,
                    start_date date not null,
                    end_date date,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_labor_classification_employee foreign key (employee_id) references employee.employee(id),
                    constraint uk_labor_classification_business unique (employee_id, start_date),
                    constraint chk_labor_classification_dates check (end_date is null or start_date <= end_date)
                );

                create table employee.contract (
                    id bigint generated always as identity primary key,
                    employee_id bigint not null,
                    contract_code varchar(30) not null,
                    contract_subtype_code varchar(30) not null,
                    start_date date not null,
                    end_date date,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_contract_employee foreign key (employee_id) references employee.employee(id),
                    constraint uk_contract_business unique (employee_id, start_date),
                    constraint chk_contract_dates check (end_date is null or start_date <= end_date)
                );

                create table employee.work_center (
                    id bigint generated always as identity primary key,
                    employee_id bigint not null,
                    work_center_assignment_number integer not null,
                    work_center_code varchar(30) not null,
                    start_date date not null,
                    end_date date,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_work_center_employee foreign key (employee_id) references employee.employee(id),
                    constraint uk_work_center_assignment_number unique (employee_id, work_center_assignment_number),
                    constraint chk_work_center_dates check (end_date is null or start_date <= end_date)
                );

                create table employee.working_time (
                    id bigint generated always as identity primary key,
                    employee_id bigint not null,
                    working_time_number integer not null,
                    start_date date not null,
                    end_date date,
                    working_time_percentage numeric(5,2) not null,
                    weekly_hours numeric(6,2) not null,
                    daily_hours numeric(6,2) not null,
                    monthly_hours numeric(6,2) not null,
                    created_at timestamp not null default now(),
                    updated_at timestamp not null default now(),
                    constraint fk_working_time_employee foreign key (employee_id) references employee.employee(id),
                    constraint uk_working_time_number unique (employee_id, working_time_number),
                    constraint chk_working_time_dates check (end_date is null or start_date <= end_date)
                );

                insert into rulesystem.rule_entity_type (code, name, active) values
                    ('COMPANY', 'Company', true),
                    ('WORK_CENTER', 'Work Center', true),
                    ('COST_CENTER', 'Cost Center', true),
                    ('AGREEMENT', 'Agreement', true),
                    ('AGREEMENT_CATEGORY', 'Agreement Category', true),
                    ('CONTRACT', 'Contract', true),
                    ('CONTRACT_SUBTYPE', 'Contract Subtype', true),
                    ('EMPLOYEE_PRESENCE_ENTRY_REASON', 'Employee Presence Entry Reason', true),
                    ('EMPLOYEE_PRESENCE_EXIT_REASON', 'Employee Presence Exit Reason', true);

                insert into rulesystem.rule_system (code, name, country_code, active)
                values ('ESP', 'Spain', 'ESP', true);

                insert into rulesystem.rule_entity (
                    rule_system_code,
                    rule_entity_type_code,
                    code,
                    name,
                    description,
                    active,
                    start_date,
                    end_date
                ) values
                    ('ESP', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'HIRING', 'Hiring', 'Initial hiring into the company', true, DATE '1900-01-01', null),
                    ('ESP', 'EMPLOYEE_PRESENCE_ENTRY_REASON', 'REHIRE', 'Rehire', 'Employee rehired after a previous termination', true, DATE '1900-01-01', null),
                    ('ESP', 'EMPLOYEE_PRESENCE_EXIT_REASON', 'TERMINATION', 'Termination', 'End of employment relationship', true, DATE '1900-01-01', null);

                create table rulesystem.employee_numbering_config (
                    id                  bigint generated always as identity primary key,
                    rule_system_code    varchar(20) not null,
                    prefix              varchar(14) not null default '',
                    numeric_part_length int         not null,
                    step                int         not null default 1,
                    next_value          bigint      not null default 1,
                    created_at          timestamp   not null,
                    updated_at          timestamp   not null,
                    constraint uk_employee_numbering_config_rs unique (rule_system_code),
                    constraint fk_employee_numbering_config_rs foreign key (rule_system_code) references rulesystem.rule_system(code),
                    constraint chk_employee_numbering_config_length check (length(prefix) + numeric_part_length <= 15),
                    constraint chk_employee_numbering_config_part_min check (numeric_part_length >= 1),
                    constraint chk_employee_numbering_config_step_min check (step >= 1),
                    constraint chk_employee_numbering_config_next_min check (next_value >= 1)
                );

                insert into rulesystem.employee_numbering_config
                    (rule_system_code, prefix, numeric_part_length, step, next_value, created_at, updated_at)
                values
                    ('ESP', 'EMP', 6, 1, 1, now(), now());
                """, StandardCharsets.UTF_8);
    }
}