# Employee Photo Storage Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow ADMIN users to upload, replace, and delete a profile photo for any employee; the photo is stored in MinIO via presigned PUT URL and displayed as a circular avatar throughout the app.

**Architecture:** Frontend requests a presigned PUT URL from the backend, uploads the cropped JPEG blob directly to MinIO (binary never touches the Java process), then notifies the backend with the object key to persist the public URL in `employee.photo_url`. The `employee.photo` vertical follows the same hexagonal architecture as `employee.contact`.

**Tech Stack:** MinIO (Docker), `io.minio:minio:8.5.9` SDK, Spring `@PreAuthorize("hasRole('ADMIN')")`, `ngx-image-cropper` (Angular), PrimeNG `p-dialog`, Angular signals.

---

## File Map

### Backend — new files

| File | Purpose |
|---|---|
| `src/main/resources/db/migration/V83__add_employee_photo_url.sql` | Flyway migration adds `photo_url` column |
| `com/b4rrhh/employee/photo/domain/port/EmployeePhotoStoragePort.java` | Secondary port: presigned URL, public URL, delete, extractObjectKey |
| `com/b4rrhh/employee/photo/domain/EmployeeNotFoundForPhotoException.java` | Domain exception for unknown employee |
| `com/b4rrhh/employee/photo/application/port/EmployeePhotoContext.java` | Read model returned by lookup port |
| `com/b4rrhh/employee/photo/application/port/EmployeePhotoLookupPort.java` | Lookup employee + current photoUrl by business key |
| `com/b4rrhh/employee/photo/application/port/EmployeePhotoUpdatePort.java` | Write port: set photo_url on employee row |
| `com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlCommand.java` | |
| `com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlResult.java` | |
| `com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlUseCase.java` | |
| `com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlService.java` | |
| `com/b4rrhh/employee/photo/application/usecase/ConfirmEmployeePhotoCommand.java` | |
| `com/b4rrhh/employee/photo/application/usecase/ConfirmEmployeePhotoUseCase.java` | |
| `com/b4rrhh/employee/photo/application/usecase/ConfirmEmployeePhotoService.java` | Deletes old object then persists new URL |
| `com/b4rrhh/employee/photo/application/usecase/DeleteEmployeePhotoCommand.java` | |
| `com/b4rrhh/employee/photo/application/usecase/DeleteEmployeePhotoUseCase.java` | |
| `com/b4rrhh/employee/photo/application/usecase/DeleteEmployeePhotoService.java` | |
| `com/b4rrhh/employee/photo/infrastructure/config/MinioProperties.java` | `@ConfigurationProperties(prefix="minio")` |
| `com/b4rrhh/employee/photo/infrastructure/config/MinioConfig.java` | `@Bean MinioClient` |
| `com/b4rrhh/employee/photo/infrastructure/storage/MinioEmployeePhotoStorageAdapter.java` | Implements `EmployeePhotoStoragePort` |
| `com/b4rrhh/employee/photo/infrastructure/persistence/EmployeePhotoLookupAdapter.java` | Delegates to `EmployeeOwnedLookupSupport` |
| `com/b4rrhh/employee/photo/infrastructure/persistence/EmployeePhotoUpdateAdapter.java` | JPQL update via `EntityManager` |
| `com/b4rrhh/employee/photo/infrastructure/web/dto/GeneratePhotoUploadUrlResponse.java` | |
| `com/b4rrhh/employee/photo/infrastructure/web/dto/ConfirmEmployeePhotoRequest.java` | |
| `com/b4rrhh/employee/photo/infrastructure/web/dto/EmployeePhotoErrorResponse.java` | |
| `com/b4rrhh/employee/photo/infrastructure/web/EmployeePhotoController.java` | Three endpoints, all `@PreAuthorize("hasRole('ADMIN')")` |
| `com/b4rrhh/employee/photo/infrastructure/web/EmployeePhotoExceptionHandler.java` | Catches `EmployeeNotFoundForPhotoException` |

### Backend — modified files

| File | Change |
|---|---|
| `docker/postgres/docker-compose.yaml` | Add `minio` service + `b4rrhh_minio_data` volume |
| `pom.xml` | Add `io.minio:minio:8.5.9` dependency |
| `src/main/resources/application.yml` | Add `minio:` config block |
| `com/b4rrhh/employee/employee/domain/model/Employee.java` | Add `photoUrl` field + `withPhotoUrl` + `withoutPhotoUrl` |
| `com/b4rrhh/employee/employee/infrastructure/persistence/EmployeeEntity.java` | Add `photo_url` column |
| `com/b4rrhh/employee/employee/infrastructure/web/dto/EmployeeResponse.java` | Add `String photoUrl` |
| `com/b4rrhh/employee/employee/infrastructure/web/EmployeeController.java` | Map `employee.getPhotoUrl()` in `toResponse` |
| `com/b4rrhh/employee/employee/infrastructure/web/EmployeeBusinessKeyController.java` | Map `employee.getPhotoUrl()` in `toResponse` |
| `com/b4rrhh/shared/infrastructure/config/SecurityConfig.java` | Add `@EnableMethodSecurity` |
| `openapi/personnel-administration-api.yaml` | Add `photoUrl` to `EmployeeResponse` schema + three photo endpoints |

### Frontend — new files

| File | Purpose |
|---|---|
| `src/app/features/employee/data-access/employee-photo.service.ts` | 3-step upload flow + deletePhoto |
| `src/app/features/employee/photo/employee-photo-upload-dialog.component.ts` | PrimeNG dialog + ngx-image-cropper |
| `src/app/features/employee/photo/employee-photo-upload-dialog.component.html` | |
| `src/app/features/employee/photo/employee-photo-upload-dialog.component.scss` | |

### Frontend — modified files

| File | Change |
|---|---|
| `src/app/core/auth/auth.interceptor.ts` | Skip absolute URLs (MinIO presigned) |
| `src/app/core/api/clients/employee-read.client.ts` | Map `photoUrl` in `toEmployeeReadApiModel` |
| `src/app/core/api/mappers/employee-detail.mapper.ts` | Add `photoUrl` to `EmployeeDetailReadModel` + mapper |
| `src/app/features/employee/models/employee-detail.model.ts` | Add `photoUrl: string \| null` |
| `src/app/features/employee/data-access/employee-detail-read.gateway.ts` | Pass `photoUrl` in `toEmployeeDetailModel` |
| `src/app/features/employee/identity/employee-identity-panel.component.ts` | Add `isAdmin` input, inject store + photo service, dialog signal |
| `src/app/features/employee/identity/employee-identity-panel.component.html` | Clickable avatar, dialog, delete button |
| `src/app/features/employee/shell/components/employee-detail-header.component.ts` | Read `photoUrl` from `employee()` |
| `src/app/features/employee/shell/components/employee-detail-header.component.html` | Conditional `<img>` vs initials |
| `src/app/shared/ui/entity-header/entity-header.component.ts` | Add `photoUrl` input |
| `src/app/shared/ui/entity-header/entity-header.component.html` | Conditional `<img>` vs text avatar |
| `src/app/features/employee/shell/pages/employee-detail-page.component.ts` | Pass `[isAdmin]="true"` to identity panel |

---

## Task 1: MinIO Docker Service

**Files:**
- Modify: `docker/postgres/docker-compose.yaml`

- [ ] **Step 1: Add MinIO service to docker-compose.yaml**

Replace the entire file:

```yaml
services:
  postgres:
    image: postgres:16
    container_name: b4rrhh-postgres
    restart: unless-stopped
    environment:
      POSTGRES_DB: b4rrhh
      POSTGRES_USER: b4rrhh
      POSTGRES_PASSWORD: b4rrhh
    ports:
      - "5432:5432"
    volumes:
      - b4rrhh_postgres_data:/var/lib/postgresql/data

  minio:
    image: minio/minio:latest
    container_name: b4rrhh-minio
    command: server /data --console-address ":9001"
    environment:
      MINIO_ROOT_USER: b4rrhh
      MINIO_ROOT_PASSWORD: b4rrhh123
    ports:
      - "9000:9000"
      - "9001:9001"
    volumes:
      - b4rrhh_minio_data:/data

volumes:
  b4rrhh_postgres_data:
  b4rrhh_minio_data:
```

- [ ] **Step 2: Start MinIO and create bucket**

```bash
cd b4rrhh_backend/docker/postgres
docker compose up -d
```

Open `http://localhost:9001` — login with `b4rrhh` / `b4rrhh123`.

Create bucket `b4rrhh-employee-photos` with **public read** access policy (Access Policy → Public).

Add CORS rule on the bucket: allow `PUT` from `http://localhost:4200`.

- [ ] **Step 3: Commit**

```bash
git add docker/postgres/docker-compose.yaml
git commit -m "feat: add minio service to docker-compose"
```

---

## Task 2: MinIO SDK Dependency + Application Config

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.yml`

- [ ] **Step 1: Add MinIO SDK to pom.xml**

Inside the `<dependencies>` block, after the existing dependencies, add:

```xml
<dependency>
    <groupId>io.minio</groupId>
    <artifactId>minio</artifactId>
    <version>8.5.9</version>
</dependency>
```

- [ ] **Step 2: Add minio config block to application.yml**

Append to the end of `application.yml`:

```yaml
minio:
  endpoint: http://localhost:9000
  access-key: b4rrhh
  secret-key: b4rrhh123
  bucket-name: b4rrhh-employee-photos
  presigned-url-expiry-minutes: 10
```

- [ ] **Step 3: Verify build compiles**

Run from `b4rrhh_backend/`:

```bash
mvn test -q
```

Expected: `BUILD SUCCESS` — existing tests still pass.

- [ ] **Step 4: Commit**

```bash
git add pom.xml src/main/resources/application.yml
git commit -m "feat: add minio sdk dependency and config"
```

---

## Task 3: Database Migration + Employee Domain + Entity + DTO

**Files:**
- Create: `src/main/resources/db/migration/V83__add_employee_photo_url.sql`
- Modify: `src/main/java/com/b4rrhh/employee/employee/domain/model/Employee.java`
- Modify: `src/main/java/com/b4rrhh/employee/employee/infrastructure/persistence/EmployeeEntity.java`
- Modify: `src/main/java/com/b4rrhh/employee/employee/infrastructure/web/dto/EmployeeResponse.java`
- Modify: `src/main/java/com/b4rrhh/employee/employee/infrastructure/web/EmployeeController.java`
- Modify: `src/main/java/com/b4rrhh/employee/employee/infrastructure/web/EmployeeBusinessKeyController.java`
- Test: `src/test/java/com/b4rrhh/employee/employee/domain/model/EmployeePhotoTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/b4rrhh/employee/employee/domain/model/EmployeePhotoTest.java`:

```java
package com.b4rrhh.employee.employee.domain.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

class EmployeePhotoTest {

    private Employee employeeWithNoPhoto() {
        return new Employee(1L, "ESP", "EMP", "00001",
                "Juan", "García", null, null, "ACTIVE",
                null, LocalDateTime.now(), null);
    }

    @Test
    void withPhotoUrl_returns_new_instance_with_photo_url() {
        Employee employee = employeeWithNoPhoto().withPhotoUrl("http://minio:9000/bucket/photos/key.jpg");

        assertThat(employee.getPhotoUrl()).isEqualTo("http://minio:9000/bucket/photos/key.jpg");
        assertThat(employee.getFirstName()).isEqualTo("Juan");
    }

    @Test
    void withoutPhotoUrl_clears_existing_photo() {
        Employee employee = employeeWithNoPhoto()
                .withPhotoUrl("http://minio:9000/bucket/photos/key.jpg")
                .withoutPhotoUrl();

        assertThat(employee.getPhotoUrl()).isNull();
    }

    @Test
    void new_employee_has_null_photo_url() {
        assertThat(employeeWithNoPhoto().getPhotoUrl()).isNull();
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=EmployeePhotoTest -q
```

Expected: FAIL — `Employee` constructor does not yet accept `photoUrl`.

- [ ] **Step 3: Create V83 Flyway migration**

Create `src/main/resources/db/migration/V83__add_employee_photo_url.sql`:

```sql
alter table employee.employee add column if not exists photo_url varchar(512);
```

- [ ] **Step 4: Update Employee domain model**

Replace `src/main/java/com/b4rrhh/employee/employee/domain/model/Employee.java`:

```java
package com.b4rrhh.employee.employee.domain.model;

import java.time.LocalDateTime;

public class Employee {

    private final Long id;
    private final String ruleSystemCode;
    private final String employeeTypeCode;
    private final String employeeNumber;
    private final String firstName;
    private final String lastName1;
    private final String lastName2;
    private final String preferredName;
    private final String status;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private final String photoUrl;

    public Employee(
            Long id,
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber,
            String firstName,
            String lastName1,
            String lastName2,
            String preferredName,
            String status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            String photoUrl
    ) {
        this.id = id;
        this.ruleSystemCode = ruleSystemCode;
        this.employeeTypeCode = employeeTypeCode;
        this.employeeNumber = employeeNumber;
        this.firstName = firstName;
        this.lastName1 = lastName1;
        this.lastName2 = lastName2;
        this.preferredName = preferredName;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.photoUrl = photoUrl;
    }

    public Long getId() { return id; }
    public String getRuleSystemCode() { return ruleSystemCode; }
    public String getEmployeeTypeCode() { return employeeTypeCode; }
    public String getEmployeeNumber() { return employeeNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName1() { return lastName1; }
    public String getLastName2() { return lastName2; }
    public String getPreferredName() { return preferredName; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getPhotoUrl() { return photoUrl; }

    public boolean isActive() {
        return EmployeeStatus.ACTIVE.matches(status);
    }

    public boolean isTerminated() {
        return EmployeeStatus.TERMINATED.matches(status);
    }

    public Employee withPhotoUrl(String photoUrl) {
        return new Employee(id, ruleSystemCode, employeeTypeCode, employeeNumber,
                firstName, lastName1, lastName2, preferredName, status,
                createdAt, updatedAt, photoUrl);
    }

    public Employee withoutPhotoUrl() {
        return withPhotoUrl(null);
    }

    public Employee activate() {
        return new Employee(id, ruleSystemCode, employeeTypeCode, employeeNumber,
                firstName, lastName1, lastName2, preferredName,
                EmployeeStatus.ACTIVE.name(), createdAt, LocalDateTime.now(), photoUrl);
    }

    public Employee updateIdentityFields(
            String firstName, String lastName1, String lastName2, String preferredName) {
        return new Employee(id, ruleSystemCode, employeeTypeCode, employeeNumber,
                firstName, lastName1, lastName2, preferredName, status,
                createdAt, LocalDateTime.now(), photoUrl);
    }
}
```

- [ ] **Step 5: Update EmployeeEntity**

Add after `updatedAt` field in `EmployeeEntity.java`:

```java
@Column(name = "photo_url")
private String photoUrl;
```

Add getter and setter after existing ones:

```java
public String getPhotoUrl() {
    return photoUrl;
}

public void setPhotoUrl(String photoUrl) {
    this.photoUrl = photoUrl;
}
```

- [ ] **Step 6: Update EmployeeResponse DTO**

Replace `src/main/java/com/b4rrhh/employee/employee/infrastructure/web/dto/EmployeeResponse.java`:

```java
package com.b4rrhh.employee.employee.infrastructure.web.dto;

public record EmployeeResponse(
        Long id,
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String firstName,
        String lastName1,
        String lastName2,
        String preferredName,
        String status,
        String photoUrl
) {
}
```

- [ ] **Step 7: Fix toResponse in EmployeeController**

In `EmployeeController.java`, find the `toResponse` method and replace it:

```java
private EmployeeResponse toResponse(Employee employee) {
    return new EmployeeResponse(
            employee.getId(),
            employee.getRuleSystemCode(),
            employee.getEmployeeTypeCode(),
            employee.getEmployeeNumber(),
            employee.getFirstName(),
            employee.getLastName1(),
            employee.getLastName2(),
            employee.getPreferredName(),
            employee.getStatus(),
            employee.getPhotoUrl()
    );
}
```

- [ ] **Step 8: Fix toResponse in EmployeeBusinessKeyController**

In `EmployeeBusinessKeyController.java`, same change — replace `toResponse`:

```java
private EmployeeResponse toResponse(Employee employee) {
    return new EmployeeResponse(
            employee.getId(),
            employee.getRuleSystemCode(),
            employee.getEmployeeTypeCode(),
            employee.getEmployeeNumber(),
            employee.getFirstName(),
            employee.getLastName1(),
            employee.getLastName2(),
            employee.getPreferredName(),
            employee.getStatus(),
            employee.getPhotoUrl()
    );
}
```

- [ ] **Step 9: Fix Employee constructor calls in existing tests and services**

Search for all `new Employee(` calls that pass 11 arguments and add `null` as the 12th (photoUrl):

```bash
grep -rn "new Employee(" src/ --include="*.java"
```

For each existing `new Employee(...)` call, add `, null` before the closing `)` to provide `null` for `photoUrl`. Do the same for any test or factory that constructs `Employee` instances.

- [ ] **Step 10: Run tests**

```bash
mvn test -q
```

Expected: `BUILD SUCCESS` — `EmployeePhotoTest` passes, all existing tests pass.

- [ ] **Step 11: Commit**

```bash
git add src/
git commit -m "feat: add photoUrl field to employee domain model, entity, and response"
```

---

## Task 4: MinIO Storage Port + Adapter

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/config/MinioProperties.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/config/MinioConfig.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/domain/port/EmployeePhotoStoragePort.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/storage/MinioEmployeePhotoStorageAdapter.java`

- [ ] **Step 1: Create MinioProperties**

```java
package com.b4rrhh.employee.photo.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        String endpoint,
        String accessKey,
        String secretKey,
        String bucketName,
        int presignedUrlExpiryMinutes
) {
}
```

- [ ] **Step 2: Create MinioConfig**

```java
package com.b4rrhh.employee.photo.infrastructure.config;

import io.minio.MinioClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {

    @Bean
    public MinioClient minioClient(MinioProperties properties) {
        return MinioClient.builder()
                .endpoint(properties.endpoint())
                .credentials(properties.accessKey(), properties.secretKey())
                .build();
    }
}
```

- [ ] **Step 3: Create EmployeePhotoStoragePort**

```java
package com.b4rrhh.employee.photo.domain.port;

public interface EmployeePhotoStoragePort {

    String generatePresignedPutUrl(String objectKey, int expiryMinutes);

    String buildPublicUrl(String objectKey);

    void deleteObject(String objectKey);

    String extractObjectKey(String publicUrl);
}
```

- [ ] **Step 4: Create MinioEmployeePhotoStorageAdapter**

```java
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
```

- [ ] **Step 5: Verify build**

```bash
mvn test -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/photo/
git commit -m "feat: add MinIO storage port and adapter for employee photos"
```

---

## Task 5: Photo Secondary Ports, Adapters, and Domain Exception

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/photo/domain/EmployeeNotFoundForPhotoException.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/port/EmployeePhotoContext.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/port/EmployeePhotoLookupPort.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/persistence/EmployeePhotoLookupAdapter.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/port/EmployeePhotoUpdatePort.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/persistence/EmployeePhotoUpdateAdapter.java`

- [ ] **Step 1: Create EmployeeNotFoundForPhotoException**

```java
package com.b4rrhh.employee.photo.domain;

public class EmployeeNotFoundForPhotoException extends RuntimeException {

    public EmployeeNotFoundForPhotoException(
            String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        super("Employee not found: " + ruleSystemCode + "/" + employeeTypeCode + "/" + employeeNumber);
    }
}
```

- [ ] **Step 2: Create EmployeePhotoContext**

```java
package com.b4rrhh.employee.photo.application.port;

public record EmployeePhotoContext(Long employeeId, String photoUrl) {
}
```

- [ ] **Step 3: Create EmployeePhotoLookupPort**

```java
package com.b4rrhh.employee.photo.application.port;

import java.util.Optional;

public interface EmployeePhotoLookupPort {

    Optional<EmployeePhotoContext> findByBusinessKey(
            String ruleSystemCode,
            String employeeTypeCode,
            String employeeNumber
    );
}
```

- [ ] **Step 4: Create EmployeePhotoLookupAdapter**

```java
package com.b4rrhh.employee.photo.infrastructure.persistence;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoContext;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoLookupPort;
import com.b4rrhh.employee.shared.infrastructure.persistence.EmployeeOwnedLookupSupport;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class EmployeePhotoLookupAdapter implements EmployeePhotoLookupPort {

    private final EmployeeOwnedLookupSupport employeeOwnedLookupSupport;

    public EmployeePhotoLookupAdapter(EmployeeOwnedLookupSupport employeeOwnedLookupSupport) {
        this.employeeOwnedLookupSupport = employeeOwnedLookupSupport;
    }

    @Override
    public Optional<EmployeePhotoContext> findByBusinessKey(
            String ruleSystemCode, String employeeTypeCode, String employeeNumber) {
        return employeeOwnedLookupSupport.findOwnedByBusinessKey(
                ruleSystemCode,
                employeeTypeCode,
                employeeNumber,
                employee -> Optional.of(
                        new EmployeePhotoContext(employee.getId(), employee.getPhotoUrl())
                )
        );
    }
}
```

- [ ] **Step 5: Create EmployeePhotoUpdatePort**

```java
package com.b4rrhh.employee.photo.application.port;

public interface EmployeePhotoUpdatePort {

    void setPhotoUrl(Long employeeId, String photoUrl);
}
```

- [ ] **Step 6: Create EmployeePhotoUpdateAdapter**

```java
package com.b4rrhh.employee.photo.infrastructure.persistence;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoUpdatePort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class EmployeePhotoUpdateAdapter implements EmployeePhotoUpdatePort {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void setPhotoUrl(Long employeeId, String photoUrl) {
        entityManager.createQuery(
                        "UPDATE EmployeeEntity e SET e.photoUrl = :photoUrl WHERE e.id = :id"
                )
                .setParameter("photoUrl", photoUrl)
                .setParameter("id", employeeId)
                .executeUpdate();
    }
}
```

- [ ] **Step 7: Verify build**

```bash
mvn test -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 8: Commit**

```bash
git add src/main/java/com/b4rrhh/employee/photo/
git commit -m "feat: add photo secondary ports, adapters, and domain exception"
```

---

## Task 6: GeneratePhotoUploadUrl Use Case

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlResult.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlService.java`
- Test: `src/test/java/com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlServiceTest.java`

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/b4rrhh/employee/photo/application/usecase/GeneratePhotoUploadUrlServiceTest.java`:

```java
package com.b4rrhh.employee.photo.application.usecase;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoContext;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoLookupPort;
import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.domain.port.EmployeePhotoStoragePort;
import com.b4rrhh.employee.photo.infrastructure.config.MinioProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=GeneratePhotoUploadUrlServiceTest -q
```

Expected: FAIL — classes do not exist yet.

- [ ] **Step 3: Create command, result, and use case interface**

`GeneratePhotoUploadUrlCommand.java`:
```java
package com.b4rrhh.employee.photo.application.usecase;

public record GeneratePhotoUploadUrlCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
```

`GeneratePhotoUploadUrlResult.java`:
```java
package com.b4rrhh.employee.photo.application.usecase;

public record GeneratePhotoUploadUrlResult(String uploadUrl, String objectKey) {
}
```

`GeneratePhotoUploadUrlUseCase.java`:
```java
package com.b4rrhh.employee.photo.application.usecase;

public interface GeneratePhotoUploadUrlUseCase {

    GeneratePhotoUploadUrlResult generate(GeneratePhotoUploadUrlCommand command);
}
```

- [ ] **Step 4: Create GeneratePhotoUploadUrlService**

```java
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
```

- [ ] **Step 5: Run test to verify it passes**

```bash
mvn test -Dtest=GeneratePhotoUploadUrlServiceTest -q
```

Expected: `BUILD SUCCESS`, 2 tests passing.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat: add GeneratePhotoUploadUrl use case"
```

---

## Task 7: ConfirmEmployeePhoto Use Case

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/ConfirmEmployeePhotoCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/ConfirmEmployeePhotoUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/ConfirmEmployeePhotoService.java`
- Test: `src/test/java/com/b4rrhh/employee/photo/application/usecase/ConfirmEmployeePhotoServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=ConfirmEmployeePhotoServiceTest -q
```

Expected: FAIL.

- [ ] **Step 3: Create command and use case interface**

`ConfirmEmployeePhotoCommand.java`:
```java
package com.b4rrhh.employee.photo.application.usecase;

public record ConfirmEmployeePhotoCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber,
        String objectKey
) {
}
```

`ConfirmEmployeePhotoUseCase.java`:
```java
package com.b4rrhh.employee.photo.application.usecase;

public interface ConfirmEmployeePhotoUseCase {

    void confirm(ConfirmEmployeePhotoCommand command);
}
```

- [ ] **Step 4: Create ConfirmEmployeePhotoService**

```java
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
```

- [ ] **Step 5: Run test to verify it passes**

```bash
mvn test -Dtest=ConfirmEmployeePhotoServiceTest -q
```

Expected: `BUILD SUCCESS`, 3 tests passing.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat: add ConfirmEmployeePhoto use case"
```

---

## Task 8: DeleteEmployeePhoto Use Case

**Files:**
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/DeleteEmployeePhotoCommand.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/DeleteEmployeePhotoUseCase.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/application/usecase/DeleteEmployeePhotoService.java`
- Test: `src/test/java/com/b4rrhh/employee/photo/application/usecase/DeleteEmployeePhotoServiceTest.java`

- [ ] **Step 1: Write the failing test**

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -Dtest=DeleteEmployeePhotoServiceTest -q
```

Expected: FAIL.

- [ ] **Step 3: Create command and use case interface**

`DeleteEmployeePhotoCommand.java`:
```java
package com.b4rrhh.employee.photo.application.usecase;

public record DeleteEmployeePhotoCommand(
        String ruleSystemCode,
        String employeeTypeCode,
        String employeeNumber
) {
}
```

`DeleteEmployeePhotoUseCase.java`:
```java
package com.b4rrhh.employee.photo.application.usecase;

public interface DeleteEmployeePhotoUseCase {

    void delete(DeleteEmployeePhotoCommand command);
}
```

- [ ] **Step 4: Create DeleteEmployeePhotoService**

```java
package com.b4rrhh.employee.photo.application.usecase;

import com.b4rrhh.employee.photo.application.port.EmployeePhotoContext;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoLookupPort;
import com.b4rrhh.employee.photo.application.port.EmployeePhotoUpdatePort;
import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.domain.port.EmployeePhotoStoragePort;
import org.springframework.stereotype.Component;

@Component
public class DeleteEmployeePhotoService implements DeleteEmployeePhotoUseCase {

    private final EmployeePhotoLookupPort lookupPort;
    private final EmployeePhotoStoragePort storagePort;
    private final EmployeePhotoUpdatePort updatePort;

    public DeleteEmployeePhotoService(
            EmployeePhotoLookupPort lookupPort,
            EmployeePhotoStoragePort storagePort,
            EmployeePhotoUpdatePort updatePort) {
        this.lookupPort = lookupPort;
        this.storagePort = storagePort;
        this.updatePort = updatePort;
    }

    @Override
    public void delete(DeleteEmployeePhotoCommand command) {
        EmployeePhotoContext context = lookupPort.findByBusinessKey(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber()
        ).orElseThrow(() -> new EmployeeNotFoundForPhotoException(
                command.ruleSystemCode(),
                command.employeeTypeCode(),
                command.employeeNumber()
        ));

        if (context.photoUrl() == null) {
            return;
        }

        String objectKey = storagePort.extractObjectKey(context.photoUrl());
        storagePort.deleteObject(objectKey);
        updatePort.setPhotoUrl(context.employeeId(), null);
    }
}
```

- [ ] **Step 5: Run test to verify it passes**

```bash
mvn test -Dtest=DeleteEmployeePhotoServiceTest -q
```

Expected: `BUILD SUCCESS`, 3 tests passing.

- [ ] **Step 6: Run all tests**

```bash
mvn test -q
```

Expected: `BUILD SUCCESS` — all tests pass.

- [ ] **Step 7: Commit**

```bash
git add src/
git commit -m "feat: add DeleteEmployeePhoto use case"
```

---

## Task 9: Web Layer — Controller, DTOs, Exception Handler

**Files:**
- Modify: `src/main/java/com/b4rrhh/shared/infrastructure/config/SecurityConfig.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/web/dto/GeneratePhotoUploadUrlResponse.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/web/dto/ConfirmEmployeePhotoRequest.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/web/dto/EmployeePhotoErrorResponse.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/web/EmployeePhotoController.java`
- Create: `src/main/java/com/b4rrhh/employee/photo/infrastructure/web/EmployeePhotoExceptionHandler.java`

- [ ] **Step 1: Enable method security in SecurityConfig**

Add `@EnableMethodSecurity` to `SecurityConfig.java`:

```java
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, DevAuthProperties.class})
@EnableMethodSecurity
public class SecurityConfig {
    // ... existing content unchanged
}
```

- [ ] **Step 2: Create DTOs**

`GeneratePhotoUploadUrlResponse.java`:
```java
package com.b4rrhh.employee.photo.infrastructure.web.dto;

public record GeneratePhotoUploadUrlResponse(String uploadUrl, String objectKey) {
}
```

`ConfirmEmployeePhotoRequest.java`:
```java
package com.b4rrhh.employee.photo.infrastructure.web.dto;

public record ConfirmEmployeePhotoRequest(String objectKey) {
}
```

`EmployeePhotoErrorResponse.java`:
```java
package com.b4rrhh.employee.photo.infrastructure.web.dto;

public record EmployeePhotoErrorResponse(String code, String message) {
}
```

- [ ] **Step 3: Create EmployeePhotoController**

```java
package com.b4rrhh.employee.photo.infrastructure.web;

import com.b4rrhh.employee.employee.application.usecase.GetEmployeeByBusinessKeyUseCase;
import com.b4rrhh.employee.employee.domain.model.Employee;
import com.b4rrhh.employee.employee.infrastructure.web.dto.EmployeeResponse;
import com.b4rrhh.employee.photo.application.usecase.*;
import com.b4rrhh.employee.photo.infrastructure.web.dto.ConfirmEmployeePhotoRequest;
import com.b4rrhh.employee.photo.infrastructure.web.dto.GeneratePhotoUploadUrlResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/photo")
public class EmployeePhotoController {

    private final GeneratePhotoUploadUrlUseCase generatePhotoUploadUrlUseCase;
    private final ConfirmEmployeePhotoUseCase confirmEmployeePhotoUseCase;
    private final DeleteEmployeePhotoUseCase deleteEmployeePhotoUseCase;
    private final GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase;

    public EmployeePhotoController(
            GeneratePhotoUploadUrlUseCase generatePhotoUploadUrlUseCase,
            ConfirmEmployeePhotoUseCase confirmEmployeePhotoUseCase,
            DeleteEmployeePhotoUseCase deleteEmployeePhotoUseCase,
            GetEmployeeByBusinessKeyUseCase getEmployeeByBusinessKeyUseCase) {
        this.generatePhotoUploadUrlUseCase = generatePhotoUploadUrlUseCase;
        this.confirmEmployeePhotoUseCase = confirmEmployeePhotoUseCase;
        this.deleteEmployeePhotoUseCase = deleteEmployeePhotoUseCase;
        this.getEmployeeByBusinessKeyUseCase = getEmployeeByBusinessKeyUseCase;
    }

    @PostMapping("/upload-url")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeneratePhotoUploadUrlResponse> generateUploadUrl(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        GeneratePhotoUploadUrlResult result = generatePhotoUploadUrlUseCase.generate(
                new GeneratePhotoUploadUrlCommand(ruleSystemCode, employeeTypeCode, employeeNumber));

        return ResponseEntity.ok(
                new GeneratePhotoUploadUrlResponse(result.uploadUrl(), result.objectKey()));
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponse> confirmPhoto(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber,
            @RequestBody ConfirmEmployeePhotoRequest request
    ) {
        confirmEmployeePhotoUseCase.confirm(
                new ConfirmEmployeePhotoCommand(
                        ruleSystemCode, employeeTypeCode, employeeNumber, request.objectKey()));

        Employee employee = getEmployeeByBusinessKeyUseCase
                .getByBusinessKey(ruleSystemCode, employeeTypeCode, employeeNumber)
                .orElseThrow();

        return ResponseEntity.ok(toResponse(employee));
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePhoto(
            @PathVariable String ruleSystemCode,
            @PathVariable String employeeTypeCode,
            @PathVariable String employeeNumber
    ) {
        deleteEmployeePhotoUseCase.delete(
                new DeleteEmployeePhotoCommand(ruleSystemCode, employeeTypeCode, employeeNumber));

        return ResponseEntity.noContent().build();
    }

    private EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getRuleSystemCode(),
                employee.getEmployeeTypeCode(),
                employee.getEmployeeNumber(),
                employee.getFirstName(),
                employee.getLastName1(),
                employee.getLastName2(),
                employee.getPreferredName(),
                employee.getStatus(),
                employee.getPhotoUrl()
        );
    }
}
```

- [ ] **Step 4: Create EmployeePhotoExceptionHandler**

```java
package com.b4rrhh.employee.photo.infrastructure.web;

import com.b4rrhh.employee.photo.domain.EmployeeNotFoundForPhotoException;
import com.b4rrhh.employee.photo.infrastructure.web.dto.EmployeePhotoErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = EmployeePhotoController.class)
public class EmployeePhotoExceptionHandler {

    @ExceptionHandler(EmployeeNotFoundForPhotoException.class)
    public ResponseEntity<EmployeePhotoErrorResponse> handleEmployeeNotFound(
            EmployeeNotFoundForPhotoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new EmployeePhotoErrorResponse("EMPLOYEE_NOT_FOUND", ex.getMessage()));
    }
}
```

- [ ] **Step 5: Run all tests**

```bash
mvn test -q
```

Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

```bash
git add src/
git commit -m "feat: add EmployeePhotoController with ADMIN role enforcement"
```

---

## Task 10: OpenAPI Spec + Frontend API Refresh

**Files:**
- Modify: `openapi/personnel-administration-api.yaml`

- [ ] **Step 1: Add photoUrl to EmployeeResponse schema**

In `openapi/personnel-administration-api.yaml`, find the `EmployeeResponse` schema (under `components.schemas`) and add `photoUrl`:

```yaml
    EmployeeResponse:
      type: object
      properties:
        id:
          type: integer
          format: int64
        ruleSystemCode:
          type: string
        employeeTypeCode:
          type: string
        employeeNumber:
          type: string
        firstName:
          type: string
        lastName1:
          type: string
        lastName2:
          type: string
          nullable: true
        preferredName:
          type: string
          nullable: true
        status:
          type: string
        photoUrl:
          type: string
          nullable: true
```

- [ ] **Step 2: Add three photo endpoints to the OpenAPI spec**

Add the following path entries. The URL pattern is `/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/photo`. Add after the existing employee paths:

```yaml
  /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/photo/upload-url:
    post:
      operationId: generatePhotoUploadUrl
      summary: Generate a presigned MinIO PUT URL for employee photo upload
      parameters:
        - name: ruleSystemCode
          in: path
          required: true
          schema:
            type: string
        - name: employeeTypeCode
          in: path
          required: true
          schema:
            type: string
        - name: employeeNumber
          in: path
          required: true
          schema:
            type: string
      responses:
        '200':
          description: Presigned upload URL and object key
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/GeneratePhotoUploadUrlResponse'

  /employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/photo:
    put:
      operationId: confirmEmployeePhoto
      summary: Confirm photo upload and persist the public URL
      parameters:
        - name: ruleSystemCode
          in: path
          required: true
          schema:
            type: string
        - name: employeeTypeCode
          in: path
          required: true
          schema:
            type: string
        - name: employeeNumber
          in: path
          required: true
          schema:
            type: string
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/ConfirmEmployeePhotoRequest'
      responses:
        '200':
          description: Updated employee with new photoUrl
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/EmployeeResponse'
    delete:
      operationId: deleteEmployeePhoto
      summary: Delete employee photo from MinIO and clear photo_url
      parameters:
        - name: ruleSystemCode
          in: path
          required: true
          schema:
            type: string
        - name: employeeTypeCode
          in: path
          required: true
          schema:
            type: string
        - name: employeeNumber
          in: path
          required: true
          schema:
            type: string
      responses:
        '204':
          description: Photo deleted
```

- [ ] **Step 3: Add new schemas to OpenAPI components**

Under `components.schemas`, add:

```yaml
    GeneratePhotoUploadUrlResponse:
      type: object
      properties:
        uploadUrl:
          type: string
        objectKey:
          type: string

    ConfirmEmployeePhotoRequest:
      type: object
      required:
        - objectKey
      properties:
        objectKey:
          type: string
```

- [ ] **Step 4: Regenerate the Angular API client**

From `b4rrhh_frontend/`:

```bash
npm run api:refresh
```

Expected: Success. Verify that `src/app/core/api/generated/model/employee-response.ts` now contains `photoUrl?: string | null`.

Also verify that `src/app/core/api/generated/api/default.service.ts` now has `generatePhotoUploadUrl(...)`, `confirmEmployeePhoto(...)`, and `deleteEmployeePhoto(...)` methods.

- [ ] **Step 5: Commit backend spec change**

```bash
git add openapi/personnel-administration-api.yaml
git commit -m "feat: add photo endpoints and photoUrl to OpenAPI spec"
```

---

## Task 11: Frontend Auth Interceptor Fix

**Files:**
- Modify: `src/app/core/auth/auth.interceptor.ts`
- Test: run existing frontend test suite

- [ ] **Step 1: Write the failing test**

Create `src/app/core/auth/auth.interceptor.spec.ts`:

```typescript
import { TestBed } from '@angular/core/testing';
import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { authInterceptor } from './auth.interceptor';
import { AuthStore } from './auth.store';

describe('authInterceptor', () => {
  let http: HttpClient;
  let controller: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
        {
          provide: AuthStore,
          useValue: { getAccessToken: () => 'test-token' },
        },
      ],
    });
    http = TestBed.inject(HttpClient);
    controller = TestBed.inject(HttpTestingController);
  });

  afterEach(() => controller.verify());

  it('adds Authorization header for relative requests', () => {
    http.get('/employees').subscribe();
    const req = controller.expectOne('/employees');
    expect(req.request.headers.get('Authorization')).toBe('Bearer test-token');
  });

  it('does NOT add Authorization header for absolute MinIO URLs', () => {
    http.put('http://localhost:9000/b4rrhh-employee-photos/photos/key.jpg', {}).subscribe();
    const req = controller.expectOne(
      'http://localhost:9000/b4rrhh-employee-photos/photos/key.jpg',
    );
    expect(req.request.headers.has('Authorization')).toBeFalse();
  });
});
```

- [ ] **Step 2: Run test to verify it fails (second test fails)**

From `b4rrhh_frontend/`:

```bash
npm run test -- --testNamePattern="does NOT add Authorization"
```

Expected: FAIL — interceptor currently adds the header to all requests.

- [ ] **Step 3: Update auth.interceptor.ts**

```typescript
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';

import { AuthStore } from './auth.store';

export const authInterceptor: HttpInterceptorFn = (request, next) => {
  if (request.url.includes('/dev/auth/token')) {
    return next(request);
  }

  if (!request.url.startsWith('/')) {
    return next(request);
  }

  const token = inject(AuthStore).getAccessToken();
  if (!token) {
    return next(request);
  }

  return next(
    request.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`,
      },
    }),
  );
};
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
npm run test
```

Expected: all tests pass including the new interceptor tests.

- [ ] **Step 5: Commit**

```bash
git add src/app/core/auth/auth.interceptor.ts src/app/core/auth/auth.interceptor.spec.ts
git commit -m "fix: skip Authorization header for absolute URLs (MinIO presigned)"
```

---

## Task 12: Frontend Employee Data Model + Mapper + Gateway

**Files:**
- Modify: `src/app/features/employee/models/employee-detail.model.ts`
- Modify: `src/app/core/api/mappers/employee-detail.mapper.ts`
- Modify: `src/app/features/employee/data-access/employee-detail-read.gateway.ts`
- Modify: `src/app/core/api/clients/employee-read.client.ts`

- [ ] **Step 1: Add photoUrl to EmployeeDetailModel**

In `employee-detail.model.ts`, add `photoUrl` to the interface:

```typescript
import { EmployeeBusinessKey } from './employee-business-key.model';

export interface EmployeeDetailModel extends EmployeeBusinessKey {
  id: number;
  firstName: string;
  lastName1: string;
  lastName2: string | null;
  preferredName: string | null;
  displayName: string;
  statusLabel: string;
  workCenter: string;
  photoUrl: string | null;
}
```

- [ ] **Step 2: Add photoUrl to EmployeeDetailReadModel and mapper**

In `employee-detail.mapper.ts`:

```typescript
import { EmployeeReadApiModel } from '../clients/employee-read.client';

export interface EmployeeDetailReadModel {
  id: number;
  ruleSystemCode: string;
  employeeTypeCode: string;
  employeeNumber: string;
  firstName: string;
  lastName1: string;
  lastName2: string | null;
  preferredName: string | null;
  displayName: string;
  statusLabel: string;
  workCenter: string;
  photoUrl: string | null;
}

const pendingWorkCenterLabel = 'Pending assignment';

export function mapEmployeeReadApiToDetailModel(source: EmployeeReadApiModel): EmployeeDetailReadModel {
  return {
    id: source.id,
    ruleSystemCode: source.ruleSystemCode,
    employeeTypeCode: source.employeeTypeCode,
    employeeNumber: source.employeeNumber,
    firstName: source.firstName,
    lastName1: source.lastName1,
    lastName2: source.lastName2,
    preferredName: source.preferredName,
    displayName: buildDisplayName(source),
    statusLabel: source.status,
    workCenter: pendingWorkCenterLabel,
    photoUrl: source.photoUrl ?? null,
  };
}

function buildDisplayName(source: EmployeeReadApiModel): string {
  const preferredName = source.preferredName?.trim();
  if (preferredName) {
    return preferredName;
  }

  const nameParts = [source.firstName, source.lastName1, source.lastName2 ?? '']
    .map((part) => part.trim())
    .filter((part) => part.length > 0);

  return nameParts.join(' ');
}
```

- [ ] **Step 3: Thread photoUrl through employee-detail-read.gateway.ts**

In `employee-detail-read.gateway.ts`, update `toEmployeeDetailModel`:

```typescript
private toEmployeeDetailModel(source: EmployeeDetailReadModel): EmployeeDetailModel {
  return {
    id: source.id,
    ruleSystemCode: source.ruleSystemCode,
    employeeTypeCode: source.employeeTypeCode,
    employeeNumber: source.employeeNumber,
    firstName: source.firstName,
    lastName1: source.lastName1,
    lastName2: source.lastName2,
    preferredName: source.preferredName,
    displayName: source.displayName,
    statusLabel: source.statusLabel,
    workCenter: source.workCenter,
    photoUrl: source.photoUrl,
  };
}
```

- [ ] **Step 4: Add photoUrl to EmployeeReadApiModel and toEmployeeReadApiModel in employee-read.client.ts**

Add `photoUrl: string | null` to the `EmployeeReadApiModel` interface:

```typescript
export interface EmployeeReadApiModel {
  id: number;
  ruleSystemCode: string;
  employeeTypeCode: string;
  employeeNumber: string;
  firstName: string;
  lastName1: string;
  lastName2: string | null;
  preferredName: string | null;
  status: string;
  photoUrl: string | null;
}
```

Update `toEmployeeReadApiModel` in the same file:

```typescript
private toEmployeeReadApiModel(source: EmployeeResponse): EmployeeReadApiModel {
  return {
    id: source.id,
    ruleSystemCode: source.ruleSystemCode,
    employeeTypeCode: source.employeeTypeCode,
    employeeNumber: source.employeeNumber,
    firstName: source.firstName,
    lastName1: source.lastName1,
    lastName2: source.lastName2 ?? null,
    preferredName: source.preferredName ?? null,
    status: source.status,
    photoUrl: source.photoUrl ?? null,
  };
}
```

- [ ] **Step 5: Run tests**

```bash
npm run test
```

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/app/core/ src/app/features/employee/models/ src/app/features/employee/data-access/employee-detail-read.gateway.ts
git commit -m "feat: thread photoUrl through employee data model and gateway"
```

---

## Task 13: EmployeePhotoService

**Files:**
- Create: `src/app/features/employee/data-access/employee-photo.service.ts`

- [ ] **Step 1: Create EmployeePhotoService**

```typescript
import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { map } from 'rxjs/operators';

import { DefaultService } from '../../../core/api/generated/api/default.service';
import { EmployeeReadApiModel } from '../../../core/api/clients/employee-read.client';
import { EmployeeResponse } from '../../../core/api/generated/model/employee-response';
import { EmployeeBusinessKey } from '../models/employee-business-key.model';

@Injectable({ providedIn: 'root' })
export class EmployeePhotoService {
  private readonly api = inject(DefaultService);
  private readonly http = inject(HttpClient);

  uploadPhoto(key: EmployeeBusinessKey, blob: Blob): Observable<EmployeeReadApiModel> {
    return this.api
      .generatePhotoUploadUrl({
        ruleSystemCode: key.ruleSystemCode,
        employeeTypeCode: key.employeeTypeCode,
        employeeNumber: key.employeeNumber,
      })
      .pipe(
        switchMap(({ uploadUrl, objectKey }) =>
          this.http
            .put(uploadUrl!, blob, { headers: { 'Content-Type': 'image/jpeg' } })
            .pipe(
              switchMap(() =>
                this.api
                  .confirmEmployeePhoto({
                    ruleSystemCode: key.ruleSystemCode,
                    employeeTypeCode: key.employeeTypeCode,
                    employeeNumber: key.employeeNumber,
                    confirmEmployeePhotoRequest: { objectKey: objectKey! },
                  })
                  .pipe(map((r) => this.toApiModel(r))),
              ),
            ),
        ),
      );
  }

  deletePhoto(key: EmployeeBusinessKey): Observable<void> {
    return this.api
      .deleteEmployeePhoto({
        ruleSystemCode: key.ruleSystemCode,
        employeeTypeCode: key.employeeTypeCode,
        employeeNumber: key.employeeNumber,
      })
      .pipe(map(() => undefined));
  }

  private toApiModel(source: EmployeeResponse): EmployeeReadApiModel {
    return {
      id: source.id!,
      ruleSystemCode: source.ruleSystemCode!,
      employeeTypeCode: source.employeeTypeCode!,
      employeeNumber: source.employeeNumber!,
      firstName: source.firstName!,
      lastName1: source.lastName1!,
      lastName2: source.lastName2 ?? null,
      preferredName: source.preferredName ?? null,
      status: source.status!,
      photoUrl: source.photoUrl ?? null,
    };
  }
}
```

- [ ] **Step 2: Run tests**

```bash
npm run test
```

Expected: all tests pass.

- [ ] **Step 3: Commit**

```bash
git add src/app/features/employee/data-access/employee-photo.service.ts
git commit -m "feat: add EmployeePhotoService with 3-step upload and deletePhoto"
```

---

## Task 14: EmployeePhotoUploadDialogComponent

**Files:**
- Create: `src/app/features/employee/photo/employee-photo-upload-dialog.component.ts`
- Create: `src/app/features/employee/photo/employee-photo-upload-dialog.component.html`
- Create: `src/app/features/employee/photo/employee-photo-upload-dialog.component.scss`

- [ ] **Step 1: Install ngx-image-cropper**

From `b4rrhh_frontend/`:

```bash
npm install ngx-image-cropper
```

Expected: installs `ngx-image-cropper` and updates `package.json`.

- [ ] **Step 2: Create the dialog component TypeScript**

```typescript
import {
  ChangeDetectionStrategy,
  Component,
  inject,
  input,
  model,
  output,
  signal,
} from '@angular/core';
import { DialogModule } from 'primeng/dialog';
import { ButtonModule } from 'primeng/button';
import { ProgressSpinnerModule } from 'primeng/progressspinner';
import { ImageCropperComponent, ImageCroppedEvent } from 'ngx-image-cropper';
import { DomSanitizer, SafeUrl } from '@angular/platform-browser';

import { EmployeePhotoService } from '../data-access/employee-photo.service';
import { EmployeeBusinessKey } from '../models/employee-business-key.model';

@Component({
  selector: 'app-employee-photo-upload-dialog',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [DialogModule, ButtonModule, ProgressSpinnerModule, ImageCropperComponent],
  templateUrl: './employee-photo-upload-dialog.component.html',
  styleUrl: './employee-photo-upload-dialog.component.scss',
})
export class EmployeePhotoUploadDialogComponent {
  readonly employeeKey = input.required<EmployeeBusinessKey>();
  readonly visible = model(false);
  readonly photoConfirmed = output<string>();

  protected readonly imageChangedEvent = signal<Event | null>(null);
  protected readonly croppedBlob = signal<Blob | null>(null);
  protected readonly uploading = signal(false);
  protected readonly uploadError = signal<string | null>(null);
  protected readonly fileTooBig = signal(false);

  private readonly photoService = inject(EmployeePhotoService);
  private readonly sanitizer = inject(DomSanitizer);

  protected onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (!input.files?.length) return;

    const file = input.files[0];
    this.fileTooBig.set(false);
    this.uploadError.set(null);

    if (file.size > 5 * 1024 * 1024) {
      this.fileTooBig.set(true);
      return;
    }

    this.imageChangedEvent.set(event);
  }

  protected onImageCropped(event: ImageCroppedEvent): void {
    this.croppedBlob.set(event.blob ?? null);
  }

  protected upload(): void {
    const blob = this.croppedBlob();
    if (!blob || this.uploading()) return;

    this.uploading.set(true);
    this.uploadError.set(null);

    this.photoService.uploadPhoto(this.employeeKey(), blob).subscribe({
      next: (employee) => {
        this.uploading.set(false);
        this.visible.set(false);
        this.imageChangedEvent.set(null);
        this.croppedBlob.set(null);
        this.photoConfirmed.emit(employee.photoUrl ?? '');
      },
      error: () => {
        this.uploading.set(false);
        this.uploadError.set('Error al subir la foto. Inténtalo de nuevo.');
      },
    });
  }

  protected cancel(): void {
    this.imageChangedEvent.set(null);
    this.croppedBlob.set(null);
    this.uploadError.set(null);
    this.visible.set(false);
  }
}
```

- [ ] **Step 3: Create the dialog HTML template**

```html
<p-dialog
  header="Subir foto de empleado"
  [modal]="true"
  [(visible)]="visible"
  [style]="{ width: '420px' }"
  (onHide)="cancel()"
>
  <div class="photo-dialog">
    @if (!imageChangedEvent()) {
      <div class="photo-dialog__select">
        <input
          #fileInput
          type="file"
          accept="image/jpeg,image/png"
          class="photo-dialog__file-input"
          (change)="onFileSelected($event)"
        />
        <p-button
          label="Seleccionar archivo"
          severity="secondary"
          [outlined]="true"
          (onClick)="fileInput.click()"
        />
        <p class="photo-dialog__hint">JPEG o PNG · máx. 5 MB</p>
        @if (fileTooBig()) {
          <p class="photo-dialog__error">El archivo supera el límite de 5 MB.</p>
        }
      </div>
    }

    @if (imageChangedEvent()) {
      <image-cropper
        [imageChangedEvent]="imageChangedEvent()"
        [maintainAspectRatio]="true"
        [aspectRatio]="1"
        [roundCropper]="true"
        [resizeToWidth]="800"
        [resizeToHeight]="800"
        format="jpeg"
        (imageCropped)="onImageCropped($event)"
      />
    }

    @if (uploadError()) {
      <p class="photo-dialog__error">{{ uploadError() }}</p>
    }
  </div>

  <ng-template pTemplate="footer">
    <p-button
      label="Cancelar"
      severity="secondary"
      [outlined]="true"
      [disabled]="uploading()"
      (onClick)="cancel()"
    />
    <p-button
      label="{{ uploading() ? 'Subiendo...' : 'Subir foto' }}"
      [disabled]="!croppedBlob() || uploading()"
      (onClick)="upload()"
    />
  </ng-template>
</p-dialog>
```

- [ ] **Step 4: Create the dialog SCSS**

```scss
.photo-dialog {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1rem;

  &__select {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 0.5rem;
    padding: 2rem 0;
  }

  &__file-input {
    display: none;
  }

  &__hint {
    font-size: 0.75rem;
    color: var(--text-color-secondary);
    margin: 0;
  }

  &__error {
    font-size: 0.75rem;
    color: var(--red-500);
    margin: 0;
  }
}
```

- [ ] **Step 5: Run tests**

```bash
npm run test
```

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/app/features/employee/photo/ package.json package-lock.json
git commit -m "feat: add EmployeePhotoUploadDialogComponent with ngx-image-cropper"
```

---

## Task 15: EmployeeIdentityPanelComponent — Clickable Avatar + Photo Dialog + Delete

**Files:**
- Modify: `src/app/features/employee/identity/employee-identity-panel.component.ts`
- Modify: `src/app/features/employee/identity/employee-identity-panel.component.html`
- Modify: `src/app/features/employee/shell/pages/employee-detail-page.component.ts`
- Modify: `src/app/features/employee/shell/pages/employee-detail-page.component.html`

- [ ] **Step 1: Update EmployeeIdentityPanelComponent TypeScript**

Replace the component class (keep imports, just update the class body):

```typescript
import { ChangeDetectionStrategy, Component, computed, inject, input, signal } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';
import { TagModule } from 'primeng/tag';
import { ButtonModule } from 'primeng/button';

import { employeeTexts } from '../employee.texts';
import { EmployeeBusinessKey } from '../models/employee-business-key.model';
import { EmployeeDetailModel } from '../models/employee-detail.model';
import {
  buildEmployeeDetailRouteCommands,
  EmployeeRouteSection,
} from '../routing/employee-route-builder.util';
import { EmployeePhotoUploadDialogComponent } from '../photo/employee-photo-upload-dialog.component';
import { EmployeePhotoService } from '../data-access/employee-photo.service';
import { EmployeeDetailStore } from '../data-access/employee-detail.store';

interface IdentityNavItem {
  section: EmployeeRouteSection;
  label: string;
  routeCommands: ReadonlyArray<string>;
}

@Component({
  selector: 'app-employee-identity-panel',
  changeDetection: ChangeDetectionStrategy.OnPush,
  imports: [RouterLink, RouterLinkActive, TagModule, ButtonModule, EmployeePhotoUploadDialogComponent],
  templateUrl: './employee-identity-panel.component.html',
  styleUrl: './employee-identity-panel.component.scss',
})
export class EmployeeIdentityPanelComponent {
  readonly employeeKey = input.required<EmployeeBusinessKey>();
  readonly employee = input<EmployeeDetailModel | null>(null);
  readonly hireDate = input<string | null>(null);
  readonly status = input<'ACTIVE' | 'TERMINATED'>('TERMINATED');
  readonly isAdmin = input(false);

  protected readonly texts = employeeTexts;
  protected readonly uploadDialogVisible = signal(false);

  private readonly photoService = inject(EmployeePhotoService);
  private readonly detailStore = inject(EmployeeDetailStore);

  protected readonly initials = computed(() => {
    const name = this.employee()?.displayName ?? '';
    const parts = name.trim().split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '?';
    if (parts.length >= 2) {
      return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase();
    }
    return parts[0].slice(0, 2).toUpperCase() || '?';
  });

  protected readonly photoUrl = computed(() => this.employee()?.photoUrl ?? null);

  protected readonly navItems = computed<ReadonlyArray<IdentityNavItem>>(() => {
    const key = this.employeeKey();
    return [
      { section: 'overview', label: this.texts.overviewNavLabel,
        routeCommands: buildEmployeeDetailRouteCommands(key, 'overview') },
      { section: 'contact', label: this.texts.personalAreaLabel,
        routeCommands: buildEmployeeDetailRouteCommands(key, 'contact') },
      { section: 'presence', label: this.texts.laborAreaLabel,
        routeCommands: buildEmployeeDetailRouteCommands(key, 'presence') },
      { section: 'organization', label: this.texts.organizationalAreaLabel,
        routeCommands: buildEmployeeDetailRouteCommands(key, 'organization') },
      { section: 'payroll', label: this.texts.payrollAreaLabel,
        routeCommands: buildEmployeeDetailRouteCommands(key, 'payroll') },
    ] as const;
  });

  protected readonly statusSeverity = computed(() =>
    this.status() === 'ACTIVE' ? 'success' : 'danger',
  );

  protected readonly statusLabel = computed(() =>
    this.status() === 'ACTIVE'
      ? this.texts.employeeStatusActiveLabel
      : this.texts.employeeStatusInactiveLabel,
  );

  protected openUploadDialog(): void {
    if (!this.isAdmin()) return;
    this.uploadDialogVisible.set(true);
  }

  protected onPhotoConfirmed(): void {
    this.detailStore.refreshEmployeeDetailByBusinessKey(this.employeeKey());
  }

  protected deletePhoto(): void {
    this.photoService.deletePhoto(this.employeeKey()).subscribe({
      next: () => this.detailStore.refreshEmployeeDetailByBusinessKey(this.employeeKey()),
    });
  }
}
```

- [ ] **Step 2: Update identity panel HTML template**

Replace `employee-identity-panel.component.html`:

```html
<div class="identity-panel">
  <!-- Avatar + name + status -->
  <div class="identity-panel__hero">
    <div
      class="identity-panel__avatar"
      [class.identity-panel__avatar--clickable]="isAdmin()"
      [attr.aria-hidden]="true"
      (click)="openUploadDialog()"
    >
      @if (photoUrl()) {
        <img class="identity-panel__avatar-photo" [src]="photoUrl()" alt="Foto del empleado" />
        @if (isAdmin()) {
          <div class="identity-panel__avatar-overlay">
            <i class="pi pi-pencil"></i>
          </div>
        }
      } @else {
        {{ initials() }}
        @if (isAdmin()) {
          <div class="identity-panel__avatar-overlay">
            <i class="pi pi-camera"></i>
          </div>
        }
      }
    </div>

    <div class="identity-panel__name-row">
      <span class="identity-panel__name">{{ employee()?.displayName ?? '—' }}</span>
      <p-tag
        class="identity-panel__status"
        [value]="statusLabel()"
        [severity]="statusSeverity()"
      />
    </div>

    @if (isAdmin() && photoUrl()) {
      <button
        class="identity-panel__delete-photo"
        type="button"
        (click)="deletePhoto()"
      >
        ✕ Eliminar foto
      </button>
    }
  </div>

  <!-- Key data fields -->
  <dl class="identity-panel__fields">
    <div class="identity-panel__field">
      <dt class="identity-panel__field-label">{{ texts.employeeConvenioLabel }}</dt>
      <dd class="identity-panel__field-value">{{ employeeKey().ruleSystemCode }}</dd>
    </div>
    <div class="identity-panel__field">
      <dt class="identity-panel__field-label">{{ texts.employeeTypeLabel }}</dt>
      <dd class="identity-panel__field-value">{{ employeeKey().employeeTypeCode }}</dd>
    </div>
    <div class="identity-panel__field">
      <dt class="identity-panel__field-label">{{ texts.employeeNumberLabel }}</dt>
      <dd class="identity-panel__field-value identity-panel__field-value--mono">{{ employeeKey().employeeNumber }}</dd>
    </div>
    @if (hireDate()) {
      <div class="identity-panel__field">
        <dt class="identity-panel__field-label">{{ texts.employeeFechaAltaLabel }}</dt>
        <dd class="identity-panel__field-value">{{ hireDate() }}</dd>
      </div>
    }
  </dl>

  <div class="identity-panel__divider"></div>

  <!-- Section navigation -->
  <nav class="identity-panel__nav" [attr.aria-label]="texts.detailNavAriaLabel">
    @for (item of navItems(); track item.section) {
      <a
        class="identity-panel__nav-item"
        [routerLink]="item.routeCommands"
        routerLinkActive="active"
        [routerLinkActiveOptions]="{ exact: true }"
      >{{ item.label }}</a>
    }
  </nav>
</div>

<!-- Photo upload dialog -->
@if (isAdmin()) {
  <app-employee-photo-upload-dialog
    [employeeKey]="employeeKey()"
    [(visible)]="uploadDialogVisible"
    (photoConfirmed)="onPhotoConfirmed()"
  />
}
```

- [ ] **Step 3: Add CSS for avatar interactions**

Append to `employee-identity-panel.component.scss`:

```scss
.identity-panel__avatar--clickable {
  cursor: pointer;
  position: relative;

  &:hover .identity-panel__avatar-overlay {
    opacity: 1;
  }
}

.identity-panel__avatar-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}

.identity-panel__avatar-overlay {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.15s ease;
  font-size: 1.1rem;
}

.identity-panel__delete-photo {
  margin-top: 0.25rem;
  font-size: 0.65rem;
  padding: 0.15rem 0.5rem;
  border-radius: 0.25rem;
  border: 1px solid var(--red-200, #fca5a5);
  background: var(--red-50, #fee2e2);
  color: var(--red-600, #dc2626);
  cursor: pointer;

  &:hover {
    background: var(--red-100, #fecaca);
  }
}
```

- [ ] **Step 4: Pass isAdmin from EmployeeDetailPageComponent**

In `employee-detail-page.component.ts`, add `isAdmin = signal(true)` (hardcoded true for ADMIN-only deployment; replace with proper JWT role check when multi-role frontend is implemented):

```typescript
// Add this protected signal near the other signals
protected readonly isAdmin = signal(true);
```

In `employee-detail-page.component.html`, find the `<app-employee-identity-panel` tag and add the `[isAdmin]` binding:

```html
<app-employee-identity-panel
  [isAdmin]="isAdmin()"
  [employeeKey]="..."
  ...
/>
```

- [ ] **Step 5: Run tests**

```bash
npm run test
```

Expected: all tests pass.

- [ ] **Step 6: Commit**

```bash
git add src/app/features/employee/identity/ src/app/features/employee/shell/pages/employee-detail-page.component.*
git commit -m "feat: make identity panel avatar clickable for photo upload (ADMIN only)"
```

---

## Task 16: Photo Display — EmployeeDetailHeader + EntityHeaderComponent

**Files:**
- Modify: `src/app/features/employee/shell/components/employee-detail-header.component.html`
- Modify: `src/app/shared/ui/entity-header/entity-header.component.ts`
- Modify: `src/app/shared/ui/entity-header/entity-header.component.html`
- Modify: `src/app/features/employee/shell/components/employee-page-header.component.ts`
- Modify: `src/app/features/employee/shell/components/employee-page-header.component.html`

- [ ] **Step 1: Update employee-detail-header avatar**

In `employee-detail-header.component.html`, find the avatar div:

```html
<div class="employee-detail-header__avatar" [attr.aria-label]="texts.detailAvatarPlaceholderAriaLabel">
  <span class="employee-detail-header__avatar-initials">{{ avatarInitials() }}</span>
</div>
```

Replace with:

```html
<div class="employee-detail-header__avatar" [attr.aria-label]="texts.detailAvatarPlaceholderAriaLabel">
  @if (employee().photoUrl) {
    <img
      class="employee-detail-header__avatar-photo"
      [src]="employee().photoUrl"
      alt="Foto del empleado"
    />
  } @else {
    <span class="employee-detail-header__avatar-initials">{{ avatarInitials() }}</span>
  }
</div>
```

- [ ] **Step 2: Add avatar-photo CSS to employee-detail-header.component.scss**

Append to `employee-detail-header.component.scss`:

```scss
.employee-detail-header__avatar-photo {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
```

- [ ] **Step 3: Add photoUrl input to EntityHeaderComponent**

In `entity-header.component.ts`, add a `photoUrl` input after `avatarText`:

```typescript
readonly avatarText = input<string | null>(null);
readonly photoUrl = input<string | null>(null);
```

- [ ] **Step 4: Update EntityHeaderComponent HTML**

In `entity-header.component.html`, find the avatar block:

```html
@if (avatarText()) {
  <div class="entity-header__avatar" aria-hidden="true">
    {{ avatarText() }}
  </div>
}
```

Replace with:

```html
@if (photoUrl()) {
  <div class="entity-header__avatar entity-header__avatar--photo" aria-hidden="true">
    <img class="entity-header__avatar-img" [src]="photoUrl()" alt="" />
  </div>
} @else if (avatarText()) {
  <div class="entity-header__avatar" aria-hidden="true">
    {{ avatarText() }}
  </div>
}
```

- [ ] **Step 5: Add avatar-photo CSS to entity-header styles**

Append to `entity-header.component.scss`:

```scss
.entity-header__avatar--photo {
  overflow: hidden;
  padding: 0;
}

.entity-header__avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 50%;
}
```

- [ ] **Step 6: Add photoUrl input to EmployeePageHeaderComponent and wire it**

In `employee-page-header.component.ts`, add a `photoUrl` input:

```typescript
readonly photoUrl = input<string | null>(null);
```

In `employee-page-header.component.html`, add `[photoUrl]="photoUrl()"` to the `<app-entity-header>` tag:

```html
<app-entity-header
  class="employee-page-header"
  [title]="fullName()"
  [subtitle]="subtitle()"
  [metadata]="metadata()"
  [status]="statusBadge()"
  [avatarText]="initials()"
  [photoUrl]="photoUrl()"
>
  ...
</app-entity-header>
```

- [ ] **Step 7: Run all tests**

```bash
npm run test
```

Expected: all tests pass.

- [ ] **Step 8: Commit**

```bash
git add src/app/features/employee/shell/components/ src/app/shared/ui/entity-header/
git commit -m "feat: display photo in employee detail header and entity header"
```

---

## Verification Checklist

Before opening a PR, verify the full flow manually:

- [ ] `docker compose up -d` — both PostgreSQL and MinIO start
- [ ] MinIO console at `http://localhost:9001` shows bucket `b4rrhh-employee-photos` with public read policy
- [ ] Backend starts without errors (`mvn spring-boot:run`)
- [ ] `mvn test` — all tests pass
- [ ] Frontend starts (`npm start`)
- [ ] Log in as ADMIN user
- [ ] Navigate to any employee detail page
- [ ] Click the avatar in the sidebar → upload dialog opens
- [ ] Select an image (< 5 MB) → cropper appears
- [ ] Click "Subir foto" → photo appears as circular avatar in sidebar and in detail header
- [ ] Reload page → photo persists
- [ ] Click avatar again → replace with different photo → old photo deleted from MinIO
- [ ] Click "✕ Eliminar foto" → avatar reverts to initials, `photo_url = null` in DB
- [ ] Non-ADMIN user cannot click the avatar (no overlay shown) — backend returns 403 for API calls
