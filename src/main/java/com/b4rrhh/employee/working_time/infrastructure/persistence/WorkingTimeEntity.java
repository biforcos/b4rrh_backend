package com.b4rrhh.employee.working_time.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "working_time", schema = "employee")
public class WorkingTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "working_time_number", nullable = false)
    private Integer workingTimeNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "working_time_percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal workingTimePercentage;

    @Column(name = "weekly_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal weeklyHours;

    @Column(name = "daily_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal dailyHours;

    @Column(name = "monthly_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal monthlyHours;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getWorkingTimeNumber() {
        return workingTimeNumber;
    }

    public void setWorkingTimeNumber(Integer workingTimeNumber) {
        this.workingTimeNumber = workingTimeNumber;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getWorkingTimePercentage() {
        return workingTimePercentage;
    }

    public void setWorkingTimePercentage(BigDecimal workingTimePercentage) {
        this.workingTimePercentage = workingTimePercentage;
    }

    public BigDecimal getWeeklyHours() {
        return weeklyHours;
    }

    public void setWeeklyHours(BigDecimal weeklyHours) {
        this.weeklyHours = weeklyHours;
    }

    public BigDecimal getDailyHours() {
        return dailyHours;
    }

    public void setDailyHours(BigDecimal dailyHours) {
        this.dailyHours = dailyHours;
    }

    public BigDecimal getMonthlyHours() {
        return monthlyHours;
    }

    public void setMonthlyHours(BigDecimal monthlyHours) {
        this.monthlyHours = monthlyHours;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}