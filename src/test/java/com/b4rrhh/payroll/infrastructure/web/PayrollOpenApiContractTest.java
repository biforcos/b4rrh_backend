package com.b4rrhh.payroll.infrastructure.web;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PayrollOpenApiContractTest {

    @Test
    void calculateEndpointIsExplicitlyDocumentedAsTemporaryStub() throws IOException {
        String contract = Files.readString(Path.of("openapi", "payroll-api.yaml"));

        assertTrue(contract.contains("/payrolls/calculate:"));
        assertTrue(contract.contains("Temporary pipeline-validation stub endpoint"));
        assertTrue(contract.contains("Temporary stub request used to materialize a payroll result during the pre-launch phase"));
        assertTrue(contract.contains("Temporary stub-provided payroll concepts"));
        assertTrue(contract.contains("Temporary stub-provided context snapshots"));
        assertTrue(contract.contains("/payroll/calculation-runs/launch:"));
        assertTrue(contract.contains("/payroll/calculation-runs/{runId}:"));
        assertTrue(contract.contains("/payroll/calculation-runs/{runId}/messages:"));
        assertTrue(contract.contains("Launch payroll calculation workflow synchronously"));
        assertTrue(contract.contains("PayrollLaunchTargetSelectionRequest"));
        assertTrue(contract.contains("List persisted operational messages for a payroll calculation run"));
        assertTrue(contract.contains("PayrollCalculationRunMessagesResponse"));
        assertTrue(contract.contains("Payroll calculation run not found"));
    }
}