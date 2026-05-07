package com.b4rrhh.employee.tax_information.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.time.LocalDate;

public class CreateEmployeeTaxInformationRequest {
    private LocalDate validFrom;
    private String familySituation;
    private int descendantsCount;
    private int ascendantsCount;
    private String disabilityDegree;
    private boolean pensionCompensatoria;
    private boolean geographicMobility;
    private boolean habitualResidenceLoan;
    private String taxTerritory;

    @JsonAnySetter
    public void rejectUnknownField(String fieldName, Object value) {
        throw new IllegalArgumentException("Unexpected field: " + fieldName);
    }

    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public String getFamilySituation() { return familySituation; }
    public void setFamilySituation(String familySituation) { this.familySituation = familySituation; }
    public int getDescendantsCount() { return descendantsCount; }
    public void setDescendantsCount(int descendantsCount) { this.descendantsCount = descendantsCount; }
    public int getAscendantsCount() { return ascendantsCount; }
    public void setAscendantsCount(int ascendantsCount) { this.ascendantsCount = ascendantsCount; }
    public String getDisabilityDegree() { return disabilityDegree; }
    public void setDisabilityDegree(String disabilityDegree) { this.disabilityDegree = disabilityDegree; }
    public boolean isPensionCompensatoria() { return pensionCompensatoria; }
    public void setPensionCompensatoria(boolean pensionCompensatoria) { this.pensionCompensatoria = pensionCompensatoria; }
    public boolean isGeographicMobility() { return geographicMobility; }
    public void setGeographicMobility(boolean geographicMobility) { this.geographicMobility = geographicMobility; }
    public boolean isHabitualResidenceLoan() { return habitualResidenceLoan; }
    public void setHabitualResidenceLoan(boolean habitualResidenceLoan) { this.habitualResidenceLoan = habitualResidenceLoan; }
    public String getTaxTerritory() { return taxTerritory; }
    public void setTaxTerritory(String taxTerritory) { this.taxTerritory = taxTerritory; }
}
