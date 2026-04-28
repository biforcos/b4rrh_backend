package com.b4rrhh.payroll_engine.concept.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
        "spring.datasource.url=jdbc:h2:mem:payroll_concept_mgmt;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
@Transactional
class PayrollConceptManagementControllerTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EXISTING_SEEDED_CONCEPT_CODE = "101";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void seedConcept() {
        // Seed rule_system row required by FK constraints
        jdbcTemplate.update(
                "insert into rulesystem.rule_system (code, name, country_code, active, created_at, updated_at) "
                        + "values (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                RULE_SYSTEM_CODE, "Spain", "ESP", true
        );

        // Seed one PayrollObject + PayrollConcept matching the V71 seed (concept '101').
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_object "
                        + "(rule_system_code, object_type_code, object_code, created_at, updated_at) "
                        + "values (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                RULE_SYSTEM_CODE, "CONCEPT", EXISTING_SEEDED_CONCEPT_CODE
        );
        Long objectId = jdbcTemplate.queryForObject(
                "select id from payroll_engine.payroll_object "
                        + "where rule_system_code = ? and object_type_code = ? and object_code = ?",
                Long.class, RULE_SYSTEM_CODE, "CONCEPT", EXISTING_SEEDED_CONCEPT_CODE
        );
        jdbcTemplate.update(
                "insert into payroll_engine.payroll_concept "
                        + "(object_id, concept_mnemonic, calculation_type, functional_nature, "
                        + "result_composition_mode, payslip_order_code, execution_scope, "
                        + "created_at, updated_at) "
                        + "values (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                objectId, "SALARIO_BASE", "RATE_BY_QUANTITY", "EARNING",
                "REPLACE", "101", "PERIOD"
        );
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listConcepts_returnsExistingConcepts() throws Exception {
        mockMvc.perform(get("/payroll-engine/{ruleSystemCode}/concepts", RULE_SYSTEM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())))
                .andExpect(jsonPath("$[0].ruleSystemCode").value(RULE_SYSTEM_CODE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createConcept_returns201WithCreatedConcept() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("conceptCode", "TEST201");
        request.put("conceptMnemonic", "PLUS_TRANSPORTE");
        request.put("calculationType", "RATE_BY_QUANTITY");
        request.put("functionalNature", "EARNING");
        request.put("resultCompositionMode", "ACCUMULATE");
        request.put("executionScope", "SEGMENT");

        mockMvc.perform(post("/payroll-engine/{ruleSystemCode}/concepts", RULE_SYSTEM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.conceptCode").value("TEST201"))
                .andExpect(jsonPath("$.calculationType").value("RATE_BY_QUANTITY"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createConcept_returns409WhenCodeAlreadyExists() throws Exception {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("conceptCode", EXISTING_SEEDED_CONCEPT_CODE);
        request.put("conceptMnemonic", "DUPLICATE");
        request.put("calculationType", "DIRECT_AMOUNT");
        request.put("functionalNature", "TECHNICAL");
        request.put("resultCompositionMode", "REPLACE");
        request.put("executionScope", "SEGMENT");

        mockMvc.perform(post("/payroll-engine/{ruleSystemCode}/concepts", RULE_SYSTEM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
