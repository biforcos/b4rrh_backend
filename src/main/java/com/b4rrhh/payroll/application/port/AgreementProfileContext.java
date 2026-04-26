package com.b4rrhh.payroll.application.port;

import java.math.BigDecimal;

public record AgreementProfileContext(
        String officialAgreementNumber,
        String displayName,
        String shortName,
        BigDecimal annualHours
) {}
