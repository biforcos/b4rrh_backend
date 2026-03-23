package com.b4rrhh.employee.contract.application.usecase;

import com.b4rrhh.employee.contract.application.command.ListContractSubtypeCatalogCommand;
import com.b4rrhh.employee.contract.application.model.ContractSubtypeCatalogItem;
import com.b4rrhh.employee.contract.application.port.ContractSubtypeCatalogLookupPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ListContractSubtypeCatalogService implements ListContractSubtypeCatalogUseCase {

    private final ContractSubtypeCatalogLookupPort contractSubtypeCatalogLookupPort;

    public ListContractSubtypeCatalogService(ContractSubtypeCatalogLookupPort contractSubtypeCatalogLookupPort) {
        this.contractSubtypeCatalogLookupPort = contractSubtypeCatalogLookupPort;
    }

    @Override
    public List<ContractSubtypeCatalogItem> list(ListContractSubtypeCatalogCommand command) {
        String normalizedRuleSystemCode = normalizeRequiredCode(command.ruleSystemCode());
        String normalizedContractTypeCode = normalizeRequiredCode(command.contractTypeCode());
        LocalDate referenceDate = command.referenceDate();

        if (referenceDate == null) {
            return contractSubtypeCatalogLookupPort.listActiveSubtypesByContractType(
                    normalizedRuleSystemCode,
                    normalizedContractTypeCode
            );
        }

        return contractSubtypeCatalogLookupPort.listActiveSubtypesByContractTypeOnDate(
                normalizedRuleSystemCode,
                normalizedContractTypeCode,
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