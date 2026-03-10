package com.b4rrhh.rulesystem.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RuleEntity {

    private final Long id;
    private final String ruleSystemCode;
    private final String ruleEntityTypeCode;
    private final String code;
    private final String name;
    private final String description;
    private final boolean active;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RuleEntity(
            Long id,
            String ruleSystemCode,
            String ruleEntityTypeCode,
            String code,
            String name,
            String description,
            boolean active,
            LocalDate startDate,
            LocalDate endDate,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.ruleSystemCode = ruleSystemCode;
        this.ruleEntityTypeCode = ruleEntityTypeCode;
        this.code = code;
        this.name = name;
        this.description = description;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getRuleEntityTypeCode() { return ruleEntityTypeCode; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
