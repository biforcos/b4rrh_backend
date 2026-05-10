package com.b4rrhh.employee.lifecycle.application.port;

import com.b4rrhh.employee.lifecycle.application.model.HireContext;

public interface HireParticipant {
    // Execution order. 10=EmployeeCore, 20=Presence, 30=WorkCenter, 40=CostCenter,
    // 50=Contract, 60=LaborClassification, 70=WorkingTime
    int order();
    void participate(HireContext ctx);
}
