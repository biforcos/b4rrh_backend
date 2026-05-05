package com.b4rrhh.payroll.infrastructure.web;

import com.b4rrhh.payroll.domain.exception.PayrollTypeInvalidException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollExceptionHandlerTest {

    private final PayrollExceptionHandler handler = new PayrollExceptionHandler();

    @Test
    void mapsPayrollTypeInvalidExceptionToBadRequest() {
        var ex = new PayrollTypeInvalidException("ORD");

        var response = handler.handleBadRequest(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid payrollTypeCode: 'ORD'", response.getBody().message());
    }
}
