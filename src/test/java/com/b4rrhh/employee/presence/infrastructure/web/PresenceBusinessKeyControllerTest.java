package com.b4rrhh.employee.presence.infrastructure.web;

import com.b4rrhh.employee.presence.application.usecase.ClosePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.GetPresenceByBusinessKeyUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.domain.model.Presence;
import com.b4rrhh.employee.presence.infrastructure.web.dto.ClosePresenceRequest;
import com.b4rrhh.employee.presence.infrastructure.web.dto.CreatePresenceRequest;
import com.b4rrhh.employee.presence.infrastructure.web.dto.PresenceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceBusinessKeyControllerTest {

    @Mock
    private CreatePresenceUseCase createPresenceUseCase;
    @Mock
    private ClosePresenceUseCase closePresenceUseCase;
    @Mock
    private GetPresenceByBusinessKeyUseCase getPresenceByBusinessKeyUseCase;
    @Mock
    private ListEmployeePresencesUseCase listEmployeePresencesUseCase;

    private PresenceBusinessKeyController controller;

    @BeforeEach
    void setUp() {
        controller = new PresenceBusinessKeyController(
                createPresenceUseCase,
                closePresenceUseCase,
                getPresenceByBusinessKeyUseCase,
                listEmployeePresencesUseCase
        );
    }

    @Test
    void createsPresenceUsingEmployeeBusinessKey() {
        CreatePresenceRequest request = new CreatePresenceRequest(
                "AC01",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 10),
                null
        );

        when(createPresenceUseCase.create(any(CreatePresenceCommand.class))).thenReturn(activePresence());

        ResponseEntity<PresenceResponse> response = controller.create("ESP", "INTERNAL", "EMP001", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().presenceNumber());

        ArgumentCaptor<CreatePresenceCommand> captor = ArgumentCaptor.forClass(CreatePresenceCommand.class);
        verify(createPresenceUseCase).create(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals("AC01", captor.getValue().companyCode());
    }

    @Test
    void listsPresencesUsingEmployeeBusinessKey() {
        when(listEmployeePresencesUseCase.listByEmployeeBusinessKey("ESP", "INTERNAL", "EMP001"))
                .thenReturn(List.of(activePresence()));

        ResponseEntity<List<PresenceResponse>> response = controller.list("ESP", "INTERNAL", "EMP001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1, response.getBody().get(0).presenceNumber());
    }

    @Test
    void getsPresenceByBusinessKeyAndPresenceNumber() {
        when(getPresenceByBusinessKeyUseCase.getByBusinessKey("ESP", "INTERNAL", "EMP001", 1))
                .thenReturn(Optional.of(activePresence()));

        ResponseEntity<PresenceResponse> response = controller.getByBusinessKey("ESP", "INTERNAL", "EMP001", 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().presenceNumber());
    }

    @Test
    void closesPresenceUsingBusinessKeyAndPresenceNumber() {
        ClosePresenceRequest request = new ClosePresenceRequest(LocalDate.of(2026, 2, 1), "EXT01");
        when(closePresenceUseCase.close(any(ClosePresenceCommand.class))).thenReturn(closedPresence());

        ResponseEntity<PresenceResponse> response = controller.close("ESP", "INTERNAL", "EMP001", 1, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(LocalDate.of(2026, 2, 1), response.getBody().endDate());

        ArgumentCaptor<ClosePresenceCommand> captor = ArgumentCaptor.forClass(ClosePresenceCommand.class);
        verify(closePresenceUseCase).close(captor.capture());
        assertEquals("ESP", captor.getValue().ruleSystemCode());
        assertEquals("INTERNAL", captor.getValue().employeeTypeCode());
        assertEquals("EMP001", captor.getValue().employeeNumber());
        assertEquals(1, captor.getValue().presenceNumber());
        assertEquals("EXT01", captor.getValue().exitReasonCode());
    }

    private Presence activePresence() {
        return new Presence(
                20L,
                10L,
                1,
                "AC01",
                "ENT01",
                null,
                LocalDate.of(2026, 1, 10),
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private Presence closedPresence() {
        return new Presence(
                20L,
                10L,
                1,
                "AC01",
                "ENT01",
                "EXT01",
                LocalDate.of(2026, 1, 10),
                LocalDate.of(2026, 2, 1),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
