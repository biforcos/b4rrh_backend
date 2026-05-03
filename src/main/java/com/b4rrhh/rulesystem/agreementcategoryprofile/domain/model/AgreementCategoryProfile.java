package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.model;

public class AgreementCategoryProfile {

    private static final int GRUPO_COTIZACION_MAX_LENGTH = 2;

    private final String grupoCotizacionCode;
    private final TipoNomina tipoNomina;

    public AgreementCategoryProfile(String grupoCotizacionCode, TipoNomina tipoNomina) {
        if (grupoCotizacionCode == null || grupoCotizacionCode.trim().isEmpty()) {
            throw new IllegalArgumentException("grupoCotizacionCode is required");
        }
        String normalized = grupoCotizacionCode.trim();
        if (normalized.length() > GRUPO_COTIZACION_MAX_LENGTH) {
            throw new IllegalArgumentException("grupoCotizacionCode exceeds max length " + GRUPO_COTIZACION_MAX_LENGTH);
        }
        if (tipoNomina == null) {
            throw new IllegalArgumentException("tipoNomina is required");
        }
        this.grupoCotizacionCode = normalized;
        this.tipoNomina = tipoNomina;
    }

    public String getGrupoCotizacionCode() { return grupoCotizacionCode; }
    public TipoNomina getTipoNomina()      { return tipoNomina; }
}
