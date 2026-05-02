package com.b4rrhh.employee.photo.application.usecase;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoContext;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoLookupPort;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoUpdatePort;
import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.domain.port.EmployeePhotoStoragePort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteEmployeePhotoServiceTest {

    @Mock private EmployeePhotoLookupPort lookupPort;
    @Mock private EmployeePhotoStoragePort storagePort;
    @Mock private EmployeePhotoUpdatePort updatePort;
    @InjectMocks private DeleteEmployeePhotoService service;

    @Test
    void deletes_minio_object_and_clears_url() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "00001"))
                .thenReturn(Optional.of(new EmployeePhotoContext(1L,
                        "http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/photo.jpg")));
        when(storagePort.extractObjectKey(
                "http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/photo.jpg"))
                .thenReturn("photos/ESP/EMP/00001/photo.jpg");

        service.delete(new DeleteEmployeePhotoCommand("ESP", "EMP", "00001"));

        verify(storagePort).deleteObject("photos/ESP/EMP/00001/photo.jpg");
        verify(updatePort).setPhotoUrl(1L, null);
    }

    @Test
    void does_nothing_when_employee_has_no_photo() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "00001"))
                .thenReturn(Optional.of(new EmployeePhotoContext(1L, null)));

        service.delete(new DeleteEmployeePhotoCommand("ESP", "EMP", "00001"));

        verify(storagePort, never()).deleteObject(anyString());
        verify(updatePort, never()).setPhotoUrl(anyLong(), any());
    }

    @Test
    void throws_when_employee_not_found() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "99999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(
                new DeleteEmployeePhotoCommand("ESP", "EMP", "99999")))
                .isInstanceOf(EmployeeNotFoundForPhotoException.class);
    }
}
