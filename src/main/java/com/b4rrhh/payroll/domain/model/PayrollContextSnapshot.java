package com.b4rrhh.payroll.domain.model;

import com.b4rrhh.payroll.domain.exception.InvalidPayrollArgumentException;

public class PayrollContextSnapshot {

    private final String snapshotTypeCode;
    private final String sourceVerticalCode;
    private final String sourceBusinessKeyJson;
    private final String snapshotPayloadJson;

    public PayrollContextSnapshot(
            String snapshotTypeCode,
            String sourceVerticalCode,
            String sourceBusinessKeyJson,
            String snapshotPayloadJson
    ) {
        this.snapshotTypeCode = requireCode(snapshotTypeCode, "snapshotTypeCode", 30);
        this.sourceVerticalCode = requireCode(sourceVerticalCode, "sourceVerticalCode", 30);
        this.sourceBusinessKeyJson = requireJson(sourceBusinessKeyJson, "sourceBusinessKeyJson");
        this.snapshotPayloadJson = requireJson(snapshotPayloadJson, "snapshotPayloadJson");
    }

    private static String requireCode(String value, String fieldName, int maxLength) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPayrollArgumentException(fieldName + " is required");
        }
        String normalized = value.trim().toUpperCase();
        if (normalized.length() > maxLength) {
            throw new InvalidPayrollArgumentException(fieldName + " exceeds max length " + maxLength);
        }
        return normalized;
    }

    private static String requireJson(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new InvalidPayrollArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    public String getSnapshotTypeCode() {
        return snapshotTypeCode;
    }

    public String getSourceVerticalCode() {
        return sourceVerticalCode;
    }

    public String getSourceBusinessKeyJson() {
        return sourceBusinessKeyJson;
    }

    public String getSnapshotPayloadJson() {
        return snapshotPayloadJson;
    }
}