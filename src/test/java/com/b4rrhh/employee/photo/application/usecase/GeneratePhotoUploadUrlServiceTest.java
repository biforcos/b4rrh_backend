package com.b4rrhh.employee.photo.application.usecase;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoContext;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoLookupPort;
import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.domain.port.EmployeePhotoStoragePort;
import com.b4rrhh.employee.photo.infrastructure.config.MinioProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeneratePhotoUploadUrlServiceTest {

    @Mock
    private EmployeePhotoLookupPort lookupPort;

    @Mock
    private EmployeePhotoStoragePort storagePort;

    private final MinioProperties minioProperties =
            new MinioProperties("http://localhost:9000", "b4rrhh", "b4rrhh123",
                    "b4rrhh-employee-photos", 10);

    private GeneratePhotoUploadUrlService service() {
        return new GeneratePhotoUploadUrlService(lookupPort, storagePort, minioProperties);
    }

    @Test
    void generates_presigned_url_for_existing_employee() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "00001"))
                .thenReturn(Optional.of(new EmployeePhotoContext(1L, null)));
        when(storagePort.generatePresignedPutUrl(anyString(), eq(10)))
                .thenReturn("http://localhost:9000/presigned?sig=abc");

        GeneratePhotoUploadUrlResult result = service().generate(
                new GeneratePhotoUploadUrlCommand("ESP", "EMP", "00001"));

        assertThat(result.uploadUrl()).isEqualTo("http://localhost:9000/presigned?sig=abc");
        assertThat(result.objectKey()).matches("photos/ESP/EMP/00001/[0-9a-f\\-]+\\.jpg");
    }

    @Test
    void throws_when_employee_not_found() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "99999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().generate(
                new GeneratePhotoUploadUrlCommand("ESP", "EMP", "99999")))
                .isInstanceOf(EmployeeNotFoundForPhotoException.class);
    }
}
