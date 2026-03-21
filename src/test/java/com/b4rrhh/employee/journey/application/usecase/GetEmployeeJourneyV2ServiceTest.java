package com.b4rrhh.employee.journey.application.usecase;

import com.b4rrhh.employee.journey.application.port.EmployeeJourneyLookupPort;
import com.b4rrhh.employee.journey.application.port.JourneyContractReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyContractRecord;
import com.b4rrhh.employee.journey.application.port.JourneyEmployeeContext;
import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationRecord;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceTimelineReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceTimelineRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeJourneyV2ServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private EmployeeJourneyLookupPort employeeJourneyLookupPort;
    @Mock
    private JourneyPresenceTimelineReadPort journeyPresenceTimelineReadPort;
    @Mock
    private JourneyContractReadPort journeyContractReadPort;
    @Mock
    private JourneyLaborClassificationReadPort journeyLaborClassificationReadPort;

    private GetEmployeeJourneyV2Service service;

    @BeforeEach
    void setUp() {
        service = new GetEmployeeJourneyV2Service(
                employeeJourneyLookupPort,
                journeyPresenceTimelineReadPort,
                journeyContractReadPort,
                journeyLaborClassificationReadPort
        );
    }

    @Test
    void emitsHireForFirstPresenceStart() {
        mockEmployeeFound();
        when(journeyPresenceTimelineReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyPresenceTimelineRecord(
                        1,
                        "ES01",
                        "HIRING",
                        null,
                        LocalDate.of(2020, 1, 10),
                        null
                )
        ));
        mockNoContractOrLaborRecords();

        EmployeeJourneyTimelineView result = service.get(command());

        assertEquals(1, result.events().size());
        assertEquals(JourneyEventType.HIRE, result.events().get(0).eventType());
        assertEquals(JourneyTrackCode.PRESENCE, result.events().get(0).trackCode());
    }

    @Test
    void emitsTerminationForLastPresenceEnd() {
        mockEmployeeFound();
        when(journeyPresenceTimelineReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyPresenceTimelineRecord(
                        1,
                        "ES01",
                        "HIRING",
                        "TERM",
                        LocalDate.of(2020, 1, 10),
                        LocalDate.of(2021, 2, 1)
                )
        ));
        mockNoContractOrLaborRecords();

        EmployeeJourneyTimelineView result = service.get(command());

        assertEquals(2, result.events().size());
        assertEquals(JourneyEventType.TERMINATION, result.events().get(1).eventType());
        assertEquals(JourneyEventStatus.COMPLETED, result.events().get(1).status());
    }

    @Test
    void emitsRehireWhenPresenceStartsAfterPreviousClosure() {
        mockEmployeeFound();
        when(journeyPresenceTimelineReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyPresenceTimelineRecord(
                        1,
                        "ES01",
                        "HIRING",
                        "TERM",
                        LocalDate.of(2020, 1, 10),
                        LocalDate.of(2021, 2, 1)
                ),
                new JourneyPresenceTimelineRecord(
                        2,
                        "ES01",
                        "REHIRE",
                        null,
                        LocalDate.of(2021, 6, 1),
                        null
                )
        ));
        mockNoContractOrLaborRecords();

        EmployeeJourneyTimelineView result = service.get(command());

        assertTrue(result.events().stream().anyMatch(event -> event.eventType() == JourneyEventType.REHIRE));
    }

    @Test
    void emitsContractChangeFromSecondContract() {
        mockEmployeeFound();
        when(journeyPresenceTimelineReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyContractReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyContractRecord("IND", "FT1", LocalDate.of(2020, 1, 10), LocalDate.of(2020, 12, 31)),
                new JourneyContractRecord("TMP", "PT1", LocalDate.of(2021, 1, 1), null)
        ));
        when(journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        EmployeeJourneyTimelineView result = service.get(command());

        assertTrue(result.events().stream().anyMatch(event -> event.eventType() == JourneyEventType.CONTRACT_CHANGE));
    }

    @Test
    void emitsLaborClassificationChangeFromSecondClassification() {
        mockEmployeeFound();
        when(journeyPresenceTimelineReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyContractReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyLaborClassificationRecord("AGR_A", "CAT_A", LocalDate.of(2020, 1, 10), LocalDate.of(2020, 12, 31)),
                new JourneyLaborClassificationRecord("AGR_B", "CAT_B", LocalDate.of(2021, 1, 1), null)
        ));

        EmployeeJourneyTimelineView result = service.get(command());

        assertTrue(result.events().stream().anyMatch(
                event -> event.eventType() == JourneyEventType.LABOR_CLASSIFICATION_CHANGE
        ));
    }

    @Test
    void returnsEmptyEventsWhenEmployeeHasNoTimelineData() {
        mockEmployeeFound();
        when(journeyPresenceTimelineReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyContractReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        EmployeeJourneyTimelineView result = service.get(command());

        assertEquals("Lidia Morales", result.employee().displayName());
        assertTrue(result.events().isEmpty());
    }

    @Test
    void ordersEventsByDateThenTrackPriorityWhenDatesTie() {
        mockEmployeeFound();
        LocalDate sameDate = LocalDate.of(2020, 1, 10);

        when(journeyPresenceTimelineReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyPresenceTimelineRecord(1, "ES01", "HIRING", null, sameDate, null)
        ));
        when(journeyContractReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyContractRecord("IND", "FT1", sameDate, null)
        ));
        when(journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyLaborClassificationRecord("AGR_A", "CAT_A", sameDate, null)
        ));

        EmployeeJourneyTimelineView result = service.get(command());

        assertEquals(3, result.events().size());
        assertEquals(JourneyTrackCode.PRESENCE, result.events().get(0).trackCode());
        assertEquals(JourneyTrackCode.CONTRACT, result.events().get(1).trackCode());
        assertEquals(JourneyTrackCode.LABOR_CLASSIFICATION, result.events().get(2).trackCode());
    }

    private void mockEmployeeFound() {
        when(employeeJourneyLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(new JourneyEmployeeContext(
                        10L,
                        RULE_SYSTEM_CODE,
                        EMPLOYEE_TYPE_CODE,
                        EMPLOYEE_NUMBER,
                        "Lidia Morales"
                )));
    }

    private void mockNoContractOrLaborRecords() {
        when(journeyContractReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
    }

    private GetEmployeeJourneyV2Command command() {
        return new GetEmployeeJourneyV2Command(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER);
    }
}