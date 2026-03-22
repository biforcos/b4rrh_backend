package com.b4rrhh.employee.contract.infrastructure.rest.assembler;

import com.b4rrhh.employee.contract.application.port.ContractCatalogReadPort;
import com.b4rrhh.employee.contract.domain.model.Contract;
import com.b4rrhh.employee.contract.infrastructure.rest.dto.ContractResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ContractResponseAssembler {

    private final ContractCatalogReadPort contractCatalogReadPort;

    public ContractResponseAssembler(ContractCatalogReadPort contractCatalogReadPort) {
        this.contractCatalogReadPort = contractCatalogReadPort;
    }

    public ContractResponse toResponse(String ruleSystemCode, Contract contract) {
        String contractTypeName = contractCatalogReadPort
                .findContractTypeName(ruleSystemCode, contract.getContractCode())
                .orElse(null);
        String contractSubtypeName = contractCatalogReadPort
                .findContractSubtypeName(ruleSystemCode, contract.getContractSubtypeCode())
                .orElse(null);

        return new ContractResponse(
                contract.getContractCode(),
                contractTypeName,
                contract.getContractSubtypeCode(),
                contractSubtypeName,
                contract.getStartDate(),
                contract.getEndDate()
        );
    }

    public List<ContractResponse> toResponseList(String ruleSystemCode, List<Contract> contracts) {
        return contracts.stream()
                .map(contract -> toResponse(ruleSystemCode, contract))
                .toList();
    }
}
