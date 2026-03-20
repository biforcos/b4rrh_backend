package com.b4rrhh.employee.journey.application.usecase;

import com.b4rrhh.employee.journey.application.port.EmployeeJourneyLookupPort;
import com.b4rrhh.employee.journey.application.port.JourneyContractReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyContractRecord;
import com.b4rrhh.employee.journey.application.port.JourneyCostCenterReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyCostCenterRecord;
import com.b4rrhh.employee.journey.application.port.JourneyEmployeeContext;
import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyLaborClassificationRecord;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyPresenceRecord;
import com.b4rrhh.employee.journey.application.port.JourneyWorkCenterReadPort;
import com.b4rrhh.employee.journey.application.port.JourneyWorkCenterRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetEmployeeJourneyServiceTest {

    private static final String RULE_SYSTEM_CODE = "ESP";
    private static final String EMPLOYEE_TYPE_CODE = "INTERNAL";
    private static final String EMPLOYEE_NUMBER = "EMP001";

    @Mock
    private EmployeeJourneyLookupPort employeeJourneyLookupPort;
    @Mock
    private JourneyPresenceReadPort journeyPresenceReadPort;
    @Mock
    private JourneyContractReadPort journeyContractReadPort;
    @Mock
    private JourneyLaborClassificationReadPort journeyLaborClassificationReadPort;
    @Mock
    private JourneyWorkCenterReadPort journeyWorkCenterReadPort;
    @Mock
    private JourneyCostCenterReadPort journeyCostCenterReadPort;

    private GetEmployeeJourneyService service;

    @BeforeEach
    void setUp() {
        service = new GetEmployeeJourneyService(
                employeeJourneyLookupPort,
                journeyPresenceReadPort,
                journeyContractReadPort,
                journeyLaborClassificationReadPort,
                journeyWorkCenterReadPort,
                journeyCostCenterReadPort
        );
    }

    @Test
    void throwsWhenEmployeeDoesNotExist() {
        when(employeeJourneyLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.empty());

        assertThrows(
                JourneyEmployeeNotFoundException.class,
                () -> service.get(new GetEmployeeJourneyCommand(" esp ", " internal ", " EMP001 "))
        );

        verifyNoInteractions(
                journeyPresenceReadPort,
                journeyContractReadPort,
                journeyLaborClassificationReadPort,
                journeyWorkCenterReadPort,
                journeyCostCenterReadPort
        );
    }

    @Test
    void allowsEmptyTracksWhenNoRecordsExist() {
        when(employeeJourneyLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));
        when(journeyPresenceReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyContractReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyWorkCenterReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());
        when(journeyCostCenterReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of());

        EmployeeJourneyView result = service.get(new GetEmployeeJourneyCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        ));

        assertEquals("Lidia Morales", result.employee().displayName());
        assertEquals(5, result.tracks().size());
        assertEquals("PRESENCE", result.tracks().get(0).trackCode());
        assertEquals("CONTRACT", result.tracks().get(1).trackCode());
        assertEquals("LABOR_CLASSIFICATION", result.tracks().get(2).trackCode());
        assertEquals("WORK_CENTER", result.tracks().get(3).trackCode());
        assertEquals("COST_CENTER", result.tracks().get(4).trackCode());

        result.tracks().forEach(track -> assertEquals(0, track.items().size()));
    }

    @Test
    void mapsLabelsAndDetailsForAllV1Tracks() {
        when(employeeJourneyLookupPort.findByBusinessKey(RULE_SYSTEM_CODE, EMPLOYEE_TYPE_CODE, EMPLOYEE_NUMBER))
                .thenReturn(Optional.of(employeeContext(10L)));

        when(journeyPresenceReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyPresenceRecord(
                        "COMP01",
                        "ENTRY_A",
                        null,
                        LocalDate.of(2026, 1, 1),
                        null
                )
        ));

        when(journeyContractReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyContractRecord("IND", "FT1", LocalDate.of(2026, 1, 1), null)
        ));

        when(journeyLaborClassificationReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyLaborClassificationRecord("AGR_MAIN", "CAT_A", LocalDate.of(2026, 1, 1), null)
        ));

        when(journeyWorkCenterReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyWorkCenterRecord("MADRID_HQ", LocalDate.of(2026, 1, 1), null)
        ));

        when(journeyCostCenterReadPort.findByEmployeeIdOrderByStartDate(10L)).thenReturn(List.of(
                new JourneyCostCenterRecord("CC10", new BigDecimal("50.00"), LocalDate.of(2026, 1, 1), null),
                new JourneyCostCenterRecord("CC20", new BigDecimal("12.50"), LocalDate.of(2026, 3, 1), null)
        ));

        EmployeeJourneyView result = service.get(new GetEmployeeJourneyCommand(
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER
        ));

        JourneyItemView presenceItem = findTrack(result, "PRESENCE").items().get(0);
        assertEquals("COMP01", presenceItem.label());
        Map<String, Object> expectedPresenceDetails = new LinkedHashMap<>();
        expectedPresenceDetails.put("companyCode", "COMP01");
        expectedPresenceDetails.put("entryReasonCode", "ENTRY_A");
        expectedPresenceDetails.put("exitReasonCode", null);
        assertEquals(expectedPresenceDetails, presenceItem.details());

        JourneyItemView contractItem = findTrack(result, "CONTRACT").items().get(0);
        assertEquals("IND / FT1", contractItem.label());
        assertEquals(Map.of("contractCode", "IND", "contractSubtypeCode", "FT1"), contractItem.details());

        JourneyItemView laborItem = findTrack(result, "LABOR_CLASSIFICATION").items().get(0);
        assertEquals("AGR_MAIN / CAT_A", laborItem.label());
        assertEquals(Map.of("agreementCode", "AGR_MAIN", "agreementCategoryCode", "CAT_A"), laborItem.details());

        JourneyItemView workCenterItem = findTrack(result, "WORK_CENTER").items().get(0);
        assertEquals("MADRID_HQ", workCenterItem.label());
        assertEquals(Map.of("workCenterCode", "MADRID_HQ"), workCenterItem.details());

        List<JourneyItemView> costCenterItems = findTrack(result, "COST_CENTER").items();
        assertEquals(2, costCenterItems.size());
        assertEquals("CC10 (50%)", costCenterItems.get(0).label());
        assertEquals("CC20 (12.5%)", costCenterItems.get(1).label());
        assertEquals("CC10", costCenterItems.get(0).details().get("costCenterCode"));
        assertEquals(new BigDecimal("50.00"), costCenterItems.get(0).details().get("allocationPercentage"));
    }

    private JourneyTrackView findTrack(EmployeeJourneyView journey, String trackCode) {
        return journey.tracks().stream()
                .filter(track -> track.trackCode().equals(trackCode))
                .findFirst()
                .orElseThrow();
    }

    private JourneyEmployeeContext employeeContext(Long employeeId) {
        return new JourneyEmployeeContext(
                employeeId,
                RULE_SYSTEM_CODE,
                EMPLOYEE_TYPE_CODE,
                EMPLOYEE_NUMBER,
                "Lidia Morales"
        );
    }
}
