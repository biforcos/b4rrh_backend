package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.DeleteRuleEntityCommand;
import com.b4rrhh.rulesystem.application.usecase.DeleteRuleEntityUseCase;
import com.b4rrhh.rulesystem.domain.exception.RuleEntityInUseException;
import com.b4rrhh.rulesystem.domain.exception.RuleEntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RuleEntityBusinessKeyControllerHttpTest {

    @Mock
    private DeleteRuleEntityUseCase deleteRuleEntityUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RuleEntityBusinessKeyController controller = new RuleEntityBusinessKeyController(deleteRuleEntityUseCase);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RuleEntityExceptionHandler())
                .build();
    }

    @Test
    void deleteReturns204WhenRuleEntityCanBeDeleted() throws Exception {
        doNothing().when(deleteRuleEntityUseCase).delete(any(DeleteRuleEntityCommand.class));

        mockMvc.perform(delete("/rule-entities/ESP/EMPLOYEE_PRESENCE_COMPANY/ES01/1900-01-01"))
                .andExpect(status().isNoContent());

        ArgumentCaptor<DeleteRuleEntityCommand> captor = ArgumentCaptor.forClass(DeleteRuleEntityCommand.class);
        verify(deleteRuleEntityUseCase).delete(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("EMPLOYEE_PRESENCE_COMPANY", captor.getValue().ruleEntityTypeCode());
        assertEquals("ES01", captor.getValue().code());
        assertEquals(LocalDate.of(1900, 1, 1), captor.getValue().startDate());
    }

    @Test
    void deleteReturns404WhenRuleEntityDoesNotExist() throws Exception {
        doThrow(new RuleEntityNotFoundException("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01", LocalDate.of(1900, 1, 1)))
                .when(deleteRuleEntityUseCase)
                .delete(any(DeleteRuleEntityCommand.class));

        mockMvc.perform(delete("/rule-entities/ESP/EMPLOYEE_PRESENCE_COMPANY/ES01/1900-01-01"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void deleteReturns409WhenRuleEntityIsUsed() throws Exception {
        doThrow(new RuleEntityInUseException("ESP", "EMPLOYEE_PRESENCE_COMPANY", "ES01"))
                .when(deleteRuleEntityUseCase)
                .delete(any(DeleteRuleEntityCommand.class));

        mockMvc.perform(delete("/rule-entities/ESP/EMPLOYEE_PRESENCE_COMPANY/ES01/1900-01-01"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }
}
