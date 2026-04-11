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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Objects;

@Entity
@Table(name = "payroll_context_snapshot", schema = "payroll")
public class PayrollContextSnapshotEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "payroll_id", nullable = false)
    private PayrollEntity payroll;

    @Column(name = "snapshot_type_code", nullable = false, length = 30)
    private String snapshotTypeCode;

    @Column(name = "source_vertical_code", nullable = false, length = 30)
    private String sourceVerticalCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "source_business_key_json", nullable = false, columnDefinition = "json")
    private String sourceBusinessKeyJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "snapshot_payload_json", nullable = false, columnDefinition = "json")
    private String snapshotPayloadJson;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public PayrollEntity getPayroll() { return payroll; }
    public void setPayroll(PayrollEntity payroll) { this.payroll = payroll; }
    public String getSnapshotTypeCode() { return snapshotTypeCode; }
    public void setSnapshotTypeCode(String snapshotTypeCode) { this.snapshotTypeCode = snapshotTypeCode; }
    public String getSourceVerticalCode() { return sourceVerticalCode; }
    public void setSourceVerticalCode(String sourceVerticalCode) { this.sourceVerticalCode = sourceVerticalCode; }
    public String getSourceBusinessKeyJson() { return sourceBusinessKeyJson; }
    public void setSourceBusinessKeyJson(String sourceBusinessKeyJson) { this.sourceBusinessKeyJson = sourceBusinessKeyJson; }
    public String getSnapshotPayloadJson() { return snapshotPayloadJson; }
    public void setSnapshotPayloadJson(String snapshotPayloadJson) { this.snapshotPayloadJson = snapshotPayloadJson; }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof PayrollContextSnapshotEntity that)) {
            return false;
        }
        return Objects.equals(payroll, that.payroll)
                && Objects.equals(snapshotTypeCode, that.snapshotTypeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payroll, snapshotTypeCode);
    }
}