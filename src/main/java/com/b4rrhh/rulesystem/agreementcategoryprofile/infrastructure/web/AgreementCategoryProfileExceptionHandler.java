package com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web;

import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileCategoryNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.AgreementCategoryProfileNotFoundException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.domain.exception.GrupoCotizacionInvalidException;
import com.b4rrhh.rulesystem.agreementcategoryprofile.infrastructure.web.dto.AgreementCategoryProfileErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AgreementCategoryProfileExceptionHandler {

    @ExceptionHandler(AgreementCategoryProfileNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AgreementCategoryProfileErrorResponse handleProfileNotFound(AgreementCategoryProfileNotFoundException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(AgreementCategoryProfileCategoryNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public AgreementCategoryProfileErrorResponse handleCategoryNotFound(AgreementCategoryProfileCategoryNotFoundException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(GrupoCotizacionInvalidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public AgreementCategoryProfileErrorResponse handleGrupoInvalid(GrupoCotizacionInvalidException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public AgreementCategoryProfileErrorResponse handleBadRequest(IllegalArgumentException ex) {
        return new AgreementCategoryProfileErrorResponse(ex.getMessage());
    }
}
