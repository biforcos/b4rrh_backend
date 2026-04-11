package com.b4rrhh.payroll.infrastructure.web.dto;

public record PayrollContextSnapshotRequest(
        String snapshotTypeCode,
        String sourceVerticalCode,
        String sourceBusinessKeyJson,
        String snapshotPayloadJson
) {
}