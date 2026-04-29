package com.b4rrhh.payroll_engine.eligibility.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false",
        "spring.jpa.properties.hibernate.hbm2ddl.create_namespaces=true",
        "spring.datasource.url=jdbc:h2:mem:concept_assignment_mgmt;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password="
})
@AutoConfigureMockMvc
@Transactional
class ConceptAssignmentManagementControllerTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String CONCEPT_CODE = "SALARIO_BASE";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void seed() {
        jdbc.update("insert into rulesystem.rule_system "
                        + "(code, name, country_code, active, created_at, updated_at) "
                        + "values (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                RULE_SYSTEM_CODE, "Spain", "ESP", true);

        jdbc.update("insert into payroll_engine.payroll_object "
                        + "(rule_system_code, object_type_code, object_code, created_at, updated_at) "
                        + "values (?, 'CONCEPT', ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                RULE_SYSTEM_CODE, CONCEPT_CODE);
        Long objectId = jdbc.queryForObject(
                "select id from payroll_engine.payroll_object "
                        + "where rule_system_code = ? and object_type_code = 'CONCEPT' and object_code = ?",
                Long.class, RULE_SYSTEM_CODE, CONCEPT_CODE
        );
        jdbc.update("insert into payroll_engine.payroll_concept "
                        + "(object_id, concept_mnemonic, calculation_type, functional_nature, "
                        + "result_composition_mode, payslip_order_code, execution_scope, "
                        + "persist_to_concepts, created_at, updated_at) "
                        + "values (?, 'SB', 'DIRECT_AMOUNT', 'EARNING', 'REPLACE', '101', 'PERIOD', "
                        + "true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                objectId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listAssignments_returnsEmptyArrayWhenNoneExist() throws Exception {
        mockMvc.perform(get("/payroll-engine/{rs}/assignments", RULE_SYSTEM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAssignment_returns201WithPersistedRow() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("conceptCode", CONCEPT_CODE);
        body.put("validFrom", "2025-01-01");
        body.put("priority", 10);

        mockMvc.perform(post("/payroll-engine/{rs}/assignments", RULE_SYSTEM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.assignmentCode").value(notNullValue()))
                .andExpect(jsonPath("$.conceptCode").value(CONCEPT_CODE))
                .andExpect(jsonPath("$.priority").value(10))
                .andExpect(jsonPath("$.ruleSystemCode").value(RULE_SYSTEM_CODE));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createAssignment_returns404WhenConceptDoesNotExist() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("conceptCode", "DOES_NOT_EXIST");
        body.put("validFrom", "2025-01-01");
        body.put("priority", 0);

        mockMvc.perform(post("/payroll-engine/{rs}/assignments", RULE_SYSTEM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAssignment_returns404WhenAssignmentCodeIsUnknown() throws Exception {
        mockMvc.perform(delete("/payroll-engine/{rs}/assignments/{code}",
                        RULE_SYSTEM_CODE, "999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAssignment_returns404WhenAssignmentCodeIsNotNumeric() throws Exception {
        mockMvc.perform(delete("/payroll-engine/{rs}/assignments/{code}",
                        RULE_SYSTEM_CODE, "not-a-uuid"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteAssignment_returns204AndRemovesRow() throws Exception {
        // POST to create an assignment
        Map<String, Object> createBody = new LinkedHashMap<>();
        createBody.put("conceptCode", CONCEPT_CODE);
        createBody.put("validFrom", "2026-01-01");
        createBody.put("priority", 5);

        var result = mockMvc.perform(post("/payroll-engine/{rs}/assignments", RULE_SYSTEM_CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createBody)))
                .andExpect(status().isCreated())
                .andReturn();

        // Extract the assignmentCode from the response
        var responseBody = objectMapper.readTree(result.getResponse().getContentAsString());
        String assignmentCode = responseBody.get("assignmentCode").asText();

        // DELETE it
        mockMvc.perform(delete("/payroll-engine/{rs}/assignments/{code}",
                        RULE_SYSTEM_CODE, assignmentCode))
                .andExpect(status().isNoContent());

        // Verify it's gone
        mockMvc.perform(get("/payroll-engine/{rs}/assignments", RULE_SYSTEM_CODE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.assignmentCode == '" + assignmentCode + "')]").doesNotExist());
    }
}
