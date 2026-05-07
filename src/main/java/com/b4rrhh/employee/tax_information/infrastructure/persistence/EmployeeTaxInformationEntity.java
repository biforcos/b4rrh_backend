package com.b4rrhh.employee.tax_information.infrastructure.persistence;

import com.b4rrhh.employee.tax_information.domain.model.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(schema = "employee", name = "employee_tax_information",
    uniqueConstraints = @UniqueConstraint(columnNames = {"employee_id", "valid_from"}))
public class EmployeeTaxInformationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_situation", nullable = false)
    private FamilySituation familySituation;

    @Column(name = "descendants_count", nullable = false)
    private int descendantsCount;

    @Column(name = "ascendants_count", nullable = false)
    private int ascendantsCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "disability_degree", nullable = false)
    private DisabilityDegree disabilityDegree;

    @Column(name = "pension_compensatoria", nullable = false)
    private boolean pensionCompensatoria;

    @Column(name = "geographic_mobility", nullable = false)
    private boolean geographicMobility;

    @Column(name = "habitual_residence_loan", nullable = false)
    private boolean habitualResidenceLoan;

    @Enumerated(EnumType.STRING)
    @Column(name = "tax_territory", nullable = false)
    private TaxTerritory taxTerritory;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public FamilySituation getFamilySituation() { return familySituation; }
    public void setFamilySituation(FamilySituation familySituation) { this.familySituation = familySituation; }
    public int getDescendantsCount() { return descendantsCount; }
    public void setDescendantsCount(int descendantsCount) { this.descendantsCount = descendantsCount; }
    public int getAscendantsCount() { return ascendantsCount; }
    public void setAscendantsCount(int ascendantsCount) { this.ascendantsCount = ascendantsCount; }
    public DisabilityDegree getDisabilityDegree() { return disabilityDegree; }
    public void setDisabilityDegree(DisabilityDegree disabilityDegree) { this.disabilityDegree = disabilityDegree; }
    public boolean isPensionCompensatoria() { return pensionCompensatoria; }
    public void setPensionCompensatoria(boolean pensionCompensatoria) { this.pensionCompensatoria = pensionCompensatoria; }
    public boolean isGeographicMobility() { return geographicMobility; }
    public void setGeographicMobility(boolean geographicMobility) { this.geographicMobility = geographicMobility; }
    public boolean isHabitualResidenceLoan() { return habitualResidenceLoan; }
    public void setHabitualResidenceLoan(boolean habitualResidenceLoan) { this.habitualResidenceLoan = habitualResidenceLoan; }
    public TaxTerritory getTaxTerritory() { return taxTerritory; }
    public void setTaxTerritory(TaxTerritory taxTerritory) { this.taxTerritory = taxTerritory; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
