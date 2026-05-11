package com.b4rrhh.employee.lifecycle.application.port;

import com.b4rrhh.employee.lifecycle.application.model.TerminationContext;

public interface TerminationParticipant {
    int order();
    void participate(TerminationContext ctx);
}
