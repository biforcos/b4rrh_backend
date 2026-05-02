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
class ConfirmEmployeePhotoServiceTest {

    @Mock private EmployeePhotoLookupPort lookupPort;
    @Mock private EmployeePhotoStoragePort storagePort;
    @Mock private EmployeePhotoUpdatePort updatePort;
    @InjectMocks private ConfirmEmployeePhotoService service;

    @Test
    void persists_new_photo_url_when_no_previous_photo() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "00001"))
                .thenReturn(Optional.of(new EmployeePhotoContext(1L, null)));
        when(storagePort.buildPublicUrl("photos/ESP/EMP/00001/uuid.jpg"))
                .thenReturn("http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/uuid.jpg");

        service.confirm(new ConfirmEmployeePhotoCommand(
                "ESP", "EMP", "00001", "photos/ESP/EMP/00001/uuid.jpg"));

        verify(storagePort, never()).deleteObject(anyString());
        verify(updatePort).setPhotoUrl(1L,
                "http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/uuid.jpg");
    }

    @Test
    void deletes_old_object_before_persisting_new_url() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "00001"))
                .thenReturn(Optional.of(new EmployeePhotoContext(1L,
                        "http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/old.jpg")));
        when(storagePort.extractObjectKey(
                "http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/old.jpg"))
                .thenReturn("photos/ESP/EMP/00001/old.jpg");
        when(storagePort.buildPublicUrl("photos/ESP/EMP/00001/new.jpg"))
                .thenReturn("http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/new.jpg");

        service.confirm(new ConfirmEmployeePhotoCommand(
                "ESP", "EMP", "00001", "photos/ESP/EMP/00001/new.jpg"));

        verify(storagePort).deleteObject("photos/ESP/EMP/00001/old.jpg");
        verify(updatePort).setPhotoUrl(1L,
                "http://localhost:9000/b4rrhh-employee-photos/photos/ESP/EMP/00001/new.jpg");
    }

    @Test
    void throws_when_employee_not_found() {
        when(lookupPort.findByBusinessKey("ESP", "EMP", "99999"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.confirm(
                new ConfirmEmployeePhotoCommand("ESP", "EMP", "99999", "key.jpg")))
                .isInstanceOf(EmployeeNotFoundForPhotoException.class);
    }
}
