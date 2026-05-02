package com.b4rrhh.employee.photo.infrastructure.storage;

import com.b4rrhh.employee.photo.domain.port.EmployeePhotoStoragePort;
import com.b4rrhh.employee.photo.infrastructure.config.MinioProperties;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class MinioEmployeePhotoStorageAdapter implements EmployeePhotoStoragePort {

    private final MinioClient minioClient;
    private final MinioProperties properties;

    public MinioEmployeePhotoStorageAdapter(MinioClient minioClient, MinioProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }

    @Override
    public String generatePresignedPutUrl(String objectKey, int expiryMinutes) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.PUT)
                            .bucket(properties.bucketName())
                            .object(objectKey)
                            .expiry(expiryMinutes, TimeUnit.MINUTES)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate presigned URL for: " + objectKey, e);
        }
    }

    @Override
    public String buildPublicUrl(String objectKey) {
        return properties.endpoint() + "/" + properties.bucketName() + "/" + objectKey;
    }

    @Override
    public void deleteObject(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.bucketName())
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete MinIO object: " + objectKey, e);
        }
    }

    @Override
    public String extractObjectKey(String publicUrl) {
        String prefix = properties.endpoint() + "/" + properties.bucketName() + "/";
        return publicUrl.substring(prefix.length());
    }
}
