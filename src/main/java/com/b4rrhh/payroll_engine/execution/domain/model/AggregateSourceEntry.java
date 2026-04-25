package com.b4rrhh.payroll_engine.execution.domain.model;

import com.b4rrhh.payroll_engine.dependency.domain.model.ConceptNodeIdentity;

public record AggregateSourceEntry(ConceptNodeIdentity identity, boolean invertSign) {}
