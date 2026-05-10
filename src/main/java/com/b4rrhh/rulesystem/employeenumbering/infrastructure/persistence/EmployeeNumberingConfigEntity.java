package com.b4rrhh.rulesystem.employeenumbering.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "employee_numbering_config",
        schema = "rulesystem",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_employee_numbering_config_rs",
                columnNames = "rule_system_code"
        )
)
public class EmployeeNumberingConfigEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rule_system_code", nullable = false, length = 20)
    private String ruleSystemCode;

    @Column(name = "prefix", nullable = false, length = 14)
    private String prefix;

    @Column(name = "numeric_part_length", nullable = false)
    private int numericPartLength;

    @Column(name = "step", nullable = false)
    private int step;

    @Column(name = "next_value", nullable = false)
    private long nextValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (updatedAt == null) updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public void setRuleSystemCode(String ruleSystemCode) { this.ruleSystemCode = ruleSystemCode; }
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }
    public int getNumericPartLength() { return numericPartLength; }
    public void setNumericPartLength(int numericPartLength) { this.numericPartLength = numericPartLength; }
    public int getStep() { return step; }
    public void setStep(int step) { this.step = step; }
    public long getNextValue() { return nextValue; }
    public void setNextValue(long nextValue) { this.nextValue = nextValue; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
