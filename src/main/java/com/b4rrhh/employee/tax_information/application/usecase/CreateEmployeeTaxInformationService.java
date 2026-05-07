package com.b4rrhh.employee.tax_information.application.usecase;

import com.b4rrhh.employee.tax_information.application.port.EmployeeForTaxInfoLookupPort;
import com.b4rrhh.employee.tax_information.application.port.TaxInfoPresenceLookupPort;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationAlreadyExistsException;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationEmployeeNotFoundException;
import com.b4rrhh.employee.tax_information.domain.exception.EmployeeTaxInformationInvalidValidFromException;
import com.b4rrhh.employee.tax_information.domain.model.EmployeeTaxInformation;
import com.b4rrhh.employee.tax_information.domain.port.EmployeeTaxInformationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateEmployeeTaxInformationService implements CreateEmployeeTaxInformationUseCase {

    private final EmployeeTaxInformationRepository repo;
    private final EmployeeForTaxInfoLookupPort employeeLookupPort;
    private final TaxInfoPresenceLookupPort presenceLookupPort;

    public CreateEmployeeTaxInformationService(EmployeeTaxInformationRepository repo,
            EmployeeForTaxInfoLookupPort employeeLookupPort,
            TaxInfoPresenceLookupPort presenceLookupPort) {
        this.repo = repo;
        this.employeeLookupPort = employeeLookupPort;
        this.presenceLookupPort = presenceLookupPort;
    }

    @Override
    @Transactional
    public EmployeeTaxInformation create(CreateEmployeeTaxInformationCommand cmd) {
        String rs = cmd.ruleSystemCode().trim().toUpperCase();
        String type = cmd.employeeTypeCode().trim().toUpperCase();
        String num = cmd.employeeNumber().trim();

        Long employeeId = employeeLookupPort.findEmployeeId(rs, type, num)
            .orElseThrow(() -> new EmployeeTaxInformationEmployeeNotFoundException(rs, type, num));

        if (cmd.validFrom().getDayOfMonth() != 1
                && !presenceLookupPort.isPresenceStartDate(employeeId, cmd.validFrom())) {
            throw new EmployeeTaxInformationInvalidValidFromException(cmd.validFrom());
        }

        if (repo.findByEmployeeIdAndValidFrom(employeeId, cmd.validFrom()).isPresent()) {
            throw new EmployeeTaxInformationAlreadyExistsException(employeeId, cmd.validFrom());
        }

        return repo.save(EmployeeTaxInformation.create(employeeId, cmd.validFrom(),
            cmd.familySituation(), cmd.descendantsCount(), cmd.ascendantsCount(),
            cmd.disabilityDegree(), cmd.pensionCompensatoria(),
            cmd.geographicMobility(), cmd.habitualResidenceLoan(), cmd.taxTerritory()));
    }
}
