package com.b4rrhh.payroll.infrastructure.web.dto;

public record PayrollContextSnapshotResponse(
        String snapshotTypeCode,
        String sourceVerticalCode,
        String sourceBusinessKeyJson,
        String snapshotPayloadJson
) {
}