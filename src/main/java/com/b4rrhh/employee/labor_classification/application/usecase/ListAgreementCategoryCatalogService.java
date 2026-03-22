package com.b4rrhh.employee.labor_classification.application.usecase;

import com.b4rrhh.employee.labor_classification.application.command.ListAgreementCategoryCatalogCommand;
import com.b4rrhh.employee.labor_classification.application.model.AgreementCategoryCatalogItem;
import com.b4rrhh.employee.labor_classification.application.port.AgreementCategoryCatalogLookupPort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ListAgreementCategoryCatalogService implements ListAgreementCategoryCatalogUseCase {

    private final AgreementCategoryCatalogLookupPort agreementCategoryCatalogLookupPort;

    public ListAgreementCategoryCatalogService(AgreementCategoryCatalogLookupPort agreementCategoryCatalogLookupPort) {
        this.agreementCategoryCatalogLookupPort = agreementCategoryCatalogLookupPort;
    }

    @Override
    public List<AgreementCategoryCatalogItem> list(ListAgreementCategoryCatalogCommand command) {
        return agreementCategoryCatalogLookupPort.listActiveCategoriesByAgreement(
                normalizeRequiredCode(command.ruleSystemCode()),
                normalizeRequiredCode(command.agreementCode()),
                command.referenceDate()
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
