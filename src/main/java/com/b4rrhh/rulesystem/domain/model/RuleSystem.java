package com.b4rrhh.rulesystem.domain.model;

import java.time.LocalDateTime;

public class RuleSystem {

    private final Long id;
    private final String code;
    private String name;
    private String countryCode;
    private boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public RuleSystem(
            Long id,
            String code,
            String name,
            String countryCode,
            boolean active,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.countryCode = countryCode;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getCountryCode() { return countryCode; }
    public boolean isActive() { return active; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void update(String name, String countryCode, Boolean active) {
        this.name = name;
        this.countryCode = countryCode;
        this.active = active;
    }
}