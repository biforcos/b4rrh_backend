package com.b4rrhh.employee.photo.application.usecase;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoContext;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoLookupPort;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoUpdatePort;
import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.domain.port.EmployeePhotoStoragePort;
import org.springframework.stereotype.Component;

@Component
public class ConfirmEmployeePhotoService implements ConfirmEmployeePhotoUseCase {

    private final EmployeePhotoLookupPort lookupPort;
    private final EmployeePhotoStoragePort storagePort;
    private final EmployeePhotoUpdatePort updatePort;

    public ConfirmEmployeePhotoService(
            EmployeePhotoLookupPort lookupPort,
            EmployeePhotoStoragePort storagePort,
            EmployeePhotoUpdatePort updatePort) {
        this.lookupPort = lookupPort;
        this.storagePort = storagePort;
        this.updatePort = updatePort;
    }

    @Override
    public void confirm(ConfirmEmployeePhotoCommand command) {
        EmployeePhotoContext context = lookupPort.findByBusinessKey(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber()
        ).orElseThrow(() -> new EmployeeNotFoundForPhotoException(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber()
        ));

        if (context.photoUrl() != null) {
            String oldKey = storagePort.extractObjectKey(context.photoUrl());
            storagePort.deleteObject(oldKey);
        }

        String newPublicUrl = storagePort.buildPublicUrl(command.objectKey());
        updatePort.setPhotoUrl(context.employeeId(), newPublicUrl);
    }
}
