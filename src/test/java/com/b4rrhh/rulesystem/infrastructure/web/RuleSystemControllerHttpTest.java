package com.b4rrhh.rulesystem.infrastructure.web;

import com.b4rrhh.rulesystem.application.usecase.CreateRuleSystemUseCase;
import com.b4rrhh.rulesystem.application.usecase.GetRuleSystemByCodeUseCase;
import com.b4rrhh.rulesystem.application.usecase.ListRuleSystemsUseCase;
import com.b4rrhh.rulesystem.application.usecase.UpdateRuleSystemCommand;
import com.b4rrhh.rulesystem.application.usecase.UpdateRuleSystemUseCase;
import com.b4rrhh.rulesystem.domain.exception.RuleSystemNotFoundException;
import com.b4rrhh.rulesystem.domain.model.RuleSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RuleSystemControllerHttpTest {

    @Mock
    private CreateRuleSystemUseCase createRuleSystemUseCase;
    @Mock
    private GetRuleSystemByCodeUseCase getRuleSystemByCodeUseCase;
    @Mock
    private ListRuleSystemsUseCase listRuleSystemsUseCase;
    @Mock
    private UpdateRuleSystemUseCase updateRuleSystemUseCase;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        RuleSystemController controller = new RuleSystemController(
                createRuleSystemUseCase,
                getRuleSystemByCodeUseCase,
                listRuleSystemsUseCase,
                updateRuleSystemUseCase
        );

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new RuleSystemExceptionHandler())
                .build();
    }

    @Test
    void updatesRuleSystemByBusinessKey() throws Exception {
        when(updateRuleSystemUseCase.execute(any(UpdateRuleSystemCommand.class)))
                .thenReturn(new RuleSystem(
                        1L,
                        "ESP",
                        "Spain Updated",
                        "ESP",
                        false,
                        LocalDateTime.now(),
                        LocalDateTime.now()
                ));

        mockMvc.perform(put("/rule-systems/ESP")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Spain Updated",
                                  "countryCode": "ESP",
                                  "active": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("ESP"))
                .andExpect(jsonPath("$.name").value("Spain Updated"))
                .andExpect(jsonPath("$.countryCode").value("ESP"))
                .andExpect(jsonPath("$.active").value(false));

        ArgumentCaptor<UpdateRuleSystemCommand> captor = ArgumentCaptor.forClass(UpdateRuleSystemCommand.class);
        verify(updateRuleSystemUseCase).execute(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("Spain Updated", captor.getValue().name());
        assertEquals("ESP", captor.getValue().countryCode());
        assertEquals(false, captor.getValue().active());
    }

    @Test
    void returns404WhenRuleSystemDoesNotExist() throws Exception {
        when(updateRuleSystemUseCase.execute(any(UpdateRuleSystemCommand.class)))
                .thenThrow(new RuleSystemNotFoundException("ESP"));

        mockMvc.perform(put("/rule-systems/ESP")
                        .contentType("application/json")
                        .content("""
                                {
                                  "name": "Spain Updated",
                                  "countryCode": "ESP",
                                  "active": true
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }
}
