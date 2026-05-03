package com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception;

public class GrupoCotizacionInvalidException extends RuntimeException {
    public GrupoCotizacionInvalidException(String ruleSystemCode, String grupoCotizacionCode) {
        super("Grupo de cotización not found: " + ruleSystemCode + "/" + grupoCotizacionCode);
    }
}
