package com.b4rrhh.employee.journey.infrastructure.web;

import com.b4rrhh.employee.journey.application.usecase.EmployeeJourneyView;
import com.b4rrhh.employee.journey.application.usecase.GetEmployeeJourneyCommand;
import com.b4rrhh.employee.journey.application.usecase.GetEmployeeJourneyUseCase;
import com.b4rrhh.employee.journey.application.usecase.JourneyItemView;
import com.b4rrhh.employee.journey.application.usecase.JourneyTrackView;
import com.b4rrhh.employee.journey.infrastructure.web.dto.EmployeeJourneyResponse;
import com.b4rrhh.employee.journey.infrastructure.web.dto.JourneyEmployeeHeaderResponse;
import com.b4rrhh.employee.journey.infrastructure.web.dto.JourneyItemResponse;
import com.b4rrhh.employee.journey.infrastructure.web.dto.JourneyTrackResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/journey")
public class JourneyController {

    private final GetEmployeeJourneyUseCase getEmployeeJourneyUseCase;

    public JourneyController(GetEmployeeJourneyUseCase getEmployeeJourneyUseCase) {
        this.getEmployeeJourneyUseCase = getEmployeeJourneyUseCase;
    }

    @GetMapping
    public ResponseEntity<EmployeeJourneyResponse> getJourney(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        EmployeeJourneyView journey = getEmployeeJourneyUseCase.get(
                new GetEmployeeJourneyCommand(ruleSystemCode, employeeTypeCode, employeeNumber)
        );

        EmployeeJourneyResponse response = new EmployeeJourneyResponse(
                new JourneyEmployeeHeaderResponse(
                        journey.employee().ruleSystemCode(),
                        journey.employee().employeeTypeCode(),
                        journey.employee().employeeNumber(),
                        journey.employee().displayName()
                ),
                journey.tracks().stream()
                        .map(this::toTrackResponse)
                        .toList()
        );

        return ResponseEntity.ok(response);
    }

    private JourneyTrackResponse toTrackResponse(JourneyTrackView track) {
        return new JourneyTrackResponse(
                track.trackCode(),
                track.trackLabel(),
                track.items().stream()
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    private JourneyItemResponse toItemResponse(JourneyItemView item) {
        return new JourneyItemResponse(
                item.startDate(),
                item.endDate(),
                item.label(),
                item.details()
        );
    }
}
