package com.b4rrhh.employee.tax_information.domain.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EmployeeTaxInformation {

    public static final EmployeeTaxInformation DEFAULT = new EmployeeTaxInformation(
        null, null, null,
        FamilySituation.SINGLE_OR_OTHER, 0, 0,
        DisabilityDegree.NONE, false, false, false, TaxTerritory.COMUN,
        null, null);

    private final Long id;
    private final Long employeeId;
    private final LocalDate validFrom;
    private final FamilySituation familySituation;
    private final int descendantsCount;
    private final int ascendantsCount;
    private final DisabilityDegree disabilityDegree;
    private final boolean pensionCompensatoria;
    private final boolean geographicMobility;
    private final boolean habitualResidenceLoan;
    private final TaxTerritory taxTerritory;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private EmployeeTaxInformation(Long id, Long employeeId, LocalDate validFrom,
            FamilySituation familySituation, int descendantsCount, int ascendantsCount,
            DisabilityDegree disabilityDegree, boolean pensionCompensatoria,
            boolean geographicMobility, boolean habitualResidenceLoan, TaxTerritory taxTerritory,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.employeeId = employeeId;
        this.validFrom = validFrom;
        this.familySituation = familySituation;
        this.descendantsCount = descendantsCount;
        this.ascendantsCount = ascendantsCount;
        this.disabilityDegree = disabilityDegree;
        this.pensionCompensatoria = pensionCompensatoria;
        this.geographicMobility = geographicMobility;
        this.habitualResidenceLoan = habitualResidenceLoan;
        this.taxTerritory = taxTerritory;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static EmployeeTaxInformation create(Long employeeId, LocalDate validFrom,
            FamilySituation familySituation, int descendantsCount, int ascendantsCount,
            DisabilityDegree disabilityDegree, boolean pensionCompensatoria,
            boolean geographicMobility, boolean habitualResidenceLoan, TaxTerritory taxTerritory) {
        if (descendantsCount < 0) throw new IllegalArgumentException("descendantsCount must be >= 0");
        if (ascendantsCount < 0) throw new IllegalArgumentException("ascendantsCount must be >= 0");
        return new EmployeeTaxInformation(null, employeeId, validFrom,
            familySituation, descendantsCount, ascendantsCount,
            disabilityDegree, pensionCompensatoria, geographicMobility, habitualResidenceLoan,
            taxTerritory, null, null);
    }

    public EmployeeTaxInformation correct(FamilySituation familySituation, int descendantsCount,
            int ascendantsCount, DisabilityDegree disabilityDegree, boolean pensionCompensatoria,
            boolean geographicMobility, boolean habitualResidenceLoan, TaxTerritory taxTerritory) {
        if (descendantsCount < 0) throw new IllegalArgumentException("descendantsCount must be >= 0");
        if (ascendantsCount < 0) throw new IllegalArgumentException("ascendantsCount must be >= 0");
        return new EmployeeTaxInformation(this.id, this.employeeId, this.validFrom,
            familySituation, descendantsCount, ascendantsCount,
            disabilityDegree, pensionCompensatoria, geographicMobility, habitualResidenceLoan,
            taxTerritory, this.createdAt, null);
    }

    public static EmployeeTaxInformation rehydrate(Long id, Long employeeId, LocalDate validFrom,
            FamilySituation familySituation, int descendantsCount, int ascendantsCount,
            DisabilityDegree disabilityDegree, boolean pensionCompensatoria,
            boolean geographicMobility, boolean habitualResidenceLoan, TaxTerritory taxTerritory,
            LocalDateTime createdAt, LocalDateTime updatedAt) {
        return new EmployeeTaxInformation(id, employeeId, validFrom,
            familySituation, descendantsCount, ascendantsCount,
            disabilityDegree, pensionCompensatoria, geographicMobility, habitualResidenceLoan,
            taxTerritory, createdAt, updatedAt);
    }

    public Long getId() { return id; }
    public Long getEmployeeId() { return employeeId; }
    public LocalDate getValidFrom() { return validFrom; }
    public FamilySituation getFamilySituation() { return familySituation; }
    public int getDescendantsCount() { return descendantsCount; }
    public int getAscendantsCount() { return ascendantsCount; }
    public DisabilityDegree getDisabilityDegree() { return disabilityDegree; }
    public boolean isPensionCompensatoria() { return pensionCompensatoria; }
    public boolean isGeographicMobility() { return geographicMobility; }
    public boolean isHabitualResidenceLoan() { return habitualResidenceLoan; }
    public TaxTerritory getTaxTerritory() { return taxTerritory; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
