package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ListAgreementCategoryCatalogCommand;
import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;
import com.b4rrhh.employee.labor_classification.application.port.AgreementCategoryCatalogLookupPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListAgreementCategoryCatalogService implements ListAgreementCategoryCatalogUseCase {

    private final AgreementCategoryCatalogLookupPort agreementCategoryCatalogLookupPort;

    public ListAgreementCategoryCatalogService(AgreementCategoryCatalogLookupPort agreementCategoryCatalogLookupPort) {
        this.agreementCategoryCatalogLookupPort = agreementCategoryCatalogLookupPort;
    }

    @Override
    public List<AgreementCategoryCatalogItem> list(ListAgreementCategoryCatalogCommand command) {
        String normalizedRuleSystemCode = normalizeRequiredCode(command.ruleSystemCode());
        String normalizedAgreementCode = normalizeRequiredCode(command.agreementCode());
        LocalDate referenceDate = command.referenceDate();

        if (referenceDate == null) {
            return agreementCategoryCatalogLookupPort.listActiveCategoriesByAgreement(
                normalizedRuleSystemCode,
                normalizedAgreementCode
            );
        }

        return agreementCategoryCatalogLookupPort.listActiveCategoriesByAgreementOnDate(
            normalizedRuleSystemCode,
            normalizedAgreementCode,
            referenceDate
        );
    }

    private String normalizeRequiredCode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Required code cannot be null");
        }

        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Required code cannot be blank");
        }

        return normalized.toUpperCase();
    }
}
