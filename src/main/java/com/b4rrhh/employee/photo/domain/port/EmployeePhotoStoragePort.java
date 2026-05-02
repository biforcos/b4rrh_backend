package com.b4rrhh.employee.photo.domain.port;

public interface EmployeePhotoStoragePort {

    String generatePresignedPutUrl(String objectKey, int expiryMinutes);

    String buildPublicUrl(String objectKey);

    void deleteObject(String objectKey);

    String extractObjectKey(String publicUrl);
}
