package com.b4rrhh.employee.photo.application.usecase;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoLookupPort;
import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.domain.port.EmployeePhotoStoragePort;
import com.b4rrhh.employee.photo.infrastructure.config.MinioProperties;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class GeneratePhotoUploadUrlService implements GeneratePhotoUploadUrlUseCase {

    private final EmployeePhotoLookupPort lookupPort;
    private final EmployeePhotoStoragePort storagePort;
    private final MinioProperties minioProperties;

    public GeneratePhotoUploadUrlService(
            EmployeePhotoLookupPort lookupPort,
            EmployeePhotoStoragePort storagePort,
            MinioProperties minioProperties) {
        this.lookupPort = lookupPort;
        this.storagePort = storagePort;
        this.minioProperties = minioProperties;
    }

    @Override
    public GeneratePhotoUploadUrlResult generate(GeneratePhotoUploadUrlCommand command) {
        lookupPort.findByBusinessKey(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber()
        ).orElseThrow(() -> new EmployeeNotFoundForPhotoException(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber()
        ));

        String objectKey = "photos/" + command.ruleSystemCode()
                + "/" + command.employeeTypeCode()
                + "/" + command.employeeNumber()
                + "/" + UUID.randomUUID() + ".jpg";

        String uploadUrl = storagePort.generatePresignedPutUrl(
                objectKey, minioProperties.presignedUrlExpiryMinutes());

        return new GeneratePhotoUploadUrlResult(uploadUrl, objectKey);
    }
}
