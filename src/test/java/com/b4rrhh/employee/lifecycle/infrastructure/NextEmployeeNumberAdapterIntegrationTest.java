package com.b4rrhh.employee.lifecycle.infrastructure;

import com.b4rrhh.B4rrhhBackendApplication;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingConfigNotFoundException;
import com.b4rrhh.rulesystem.employeenumbering.domain.exception.EmployeeNumberingExhaustedException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = B4rrhhBackendApplication.class)
@Transactional
class NextEmployeeNumberAdapterIntegrationTest {

    @Autowired
    private NextEmployeeNumberAdapter adapter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private EntityManager entityManager;

    @Test
    void twoConsecutiveCallsReturnDistinctNumbers() {
        String first = adapter.consumeNext("ESP");
        String second = adapter.consumeNext("ESP");
        assertNotEquals(first, second);
    }

    @Test
    void nextValueAdvancesByStep() {
        adapter.consumeNext("ESP");
        entityManager.flush();
        Long nextValue = jdbcTemplate.queryForObject(
                "SELECT next_value FROM rulesystem.employee_numbering_config WHERE rule_system_code = 'ESP'",
                Long.class
        );
        assertEquals(2L, nextValue);
    }

    @Test
    void missingConfigThrowsNotFoundException() {
        assertThrows(EmployeeNumberingConfigNotFoundException.class,
                () -> adapter.consumeNext("NONEXISTENT"));
    }

    @Test
    void exhaustedCounterThrowsExhaustedException() {
        jdbcTemplate.update(
                "UPDATE rulesystem.employee_numbering_config SET next_value = 1000000 WHERE rule_system_code = 'ESP'"
        );
        assertThrows(EmployeeNumberingExhaustedException.class,
                () -> adapter.consumeNext("ESP"));
    }
}
