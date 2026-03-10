package com.b4rrhh.employee.presence.infrastructure.web;

import com.b4rrhh.employee.presence.application.port.EmployeePresenceContext;
import com.b4rrhh.employee.presence.application.usecase.ClosePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceCommand;
import com.b4rrhh.employee.presence.application.usecase.CreatePresenceUseCase;
import com.b4rrhh.employee.presence.application.usecase.GetPresenceByIdUseCase;
import com.b4rrhh.employee.presence.application.usecase.ListEmployeePresencesUseCase;
import com.b4rrhh.employee.presence.application.usecase.ResolveEmployeePresenceByBusinessKeyUseCase;
import com.b4rrhh.employee.presence.domain.model.Presence;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PresenceBusinessKeyControllerTest {

    @Mock
    private ResolveEmployeePresenceByBusinessKeyUseCase resolveEmployeePresenceByBusinessKeyUseCase;
    @Mock
    private CreatePresenceUseCase createPresenceUseCase;
    @Mock
    private ClosePresenceUseCase closePresenceUseCase;
    @Mock
    private GetPresenceByIdUseCase getPresenceByIdUseCase;
    @Mock
    private ListEmployeePresencesUseCase listEmployeePresencesUseCase;

    private PresenceBusinessKeyController controller;

    @BeforeEach
    void setUp() {
        controller = new PresenceBusinessKeyController(
                resolveEmployeePresenceByBusinessKeyUseCase,
                createPresenceUseCase,
                closePresenceUseCase,
                getPresenceByIdUseCase,
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

        when(resolveEmployeePresenceByBusinessKeyUseCase.resolve("ESP", "EMP001"))
                .thenReturn(new EmployeePresenceContext(10L, "ESP"));
        when(createPresenceUseCase.create(org.mockito.ArgumentMatchers.any(CreatePresenceCommand.class)))
                .thenReturn(activePresence());

        ResponseEntity<PresenceResponse> response = controller.create("ESP", "EMP001", request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().employeeId());

        ArgumentCaptor<CreatePresenceCommand> captor = ArgumentCaptor.forClass(CreatePresenceCommand.class);
        verify(createPresenceUseCase).create(captor.capture());
        assertEquals(10L, captor.getValue().employeeId());
        assertEquals("AC01", captor.getValue().companyCode());
    }

    @Test
    void listsPresencesUsingEmployeeBusinessKey() {
        when(resolveEmployeePresenceByBusinessKeyUseCase.resolve("ESP", "EMP001"))
                .thenReturn(new EmployeePresenceContext(10L, "ESP"));
        when(listEmployeePresencesUseCase.listByEmployeeId(10L)).thenReturn(List.of(activePresence()));

        ResponseEntity<List<PresenceResponse>> response = controller.list("ESP", "EMP001");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(1, response.getBody().get(0).presenceNumber());
    }

    @Test
    void getsPresenceByIdUsingEmployeeBusinessKey() {
        when(resolveEmployeePresenceByBusinessKeyUseCase.resolve("ESP", "EMP001"))
                .thenReturn(new EmployeePresenceContext(10L, "ESP"));
        when(getPresenceByIdUseCase.getById(10L, 20L)).thenReturn(Optional.of(activePresence()));

        ResponseEntity<PresenceResponse> response = controller.getById("ESP", "EMP001", 20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(20L, response.getBody().id());
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
}
