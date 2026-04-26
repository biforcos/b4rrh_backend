package com.b4rrhh.payroll.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "payroll_segment", schema = "payroll")
public class PayrollSegmentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_id", nullable = false)
    private PayrollEntity payroll;

    @Column(name = "segment_start", nullable = false)
    private LocalDate segmentStart;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PayrollEntity getPayroll() { return payroll; }
    public void setPayroll(PayrollEntity payroll) { this.payroll = payroll; }
    public LocalDate getSegmentStart() { return segmentStart; }
    public void setSegmentStart(LocalDate segmentStart) { this.segmentStart = segmentStart; }
}
