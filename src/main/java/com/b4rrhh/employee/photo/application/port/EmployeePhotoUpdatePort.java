package com.b4rrhh.employee.photo.application.port;

public interface EmployeePhotoUpdatePort {

    void setPhotoUrl(Long employeeId, String photoUrl);
}
