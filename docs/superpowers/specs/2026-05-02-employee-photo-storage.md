# Employee Photo Storage — Design Spec

**Goal:** Allow ADMIN users to upload, replace, and delete a profile photo for any employee; the photo is stored in MinIO and displayed as a circular avatar throughout the app.

**Architecture:** Presigned-upload flow — backend generates a MinIO presigned PUT URL, the browser uploads the cropped blob directly to MinIO, then notifies the backend to persist the public URL. The binary never passes through the backend Java process.

**Tech Stack:** MinIO (Docker), minio-java SDK, ngx-image-cropper (Angular), PrimeNG p-dialog, Angular HttpClient for direct PUT to presigned URL.

---

## 1. Infrastructure

### Docker (`b4rrhh_backend/docker/postgres/docker-compose.yaml`)
Add a `minio` service alongside the existing `postgres` service:
```yaml
minio:
  image: minio/minio:latest
  container_name: b4rrhh-minio
  command: server /data --console-address ":9001"
  environment:
    MINIO_ROOT_USER: b4rrhh
    MINIO_ROOT_PASSWORD: b4rrhh123
  ports:
    - "9000:9000"   # S3 API
    - "9001:9001"   # Web console
  volumes:
    - b4rrhh_minio_data:/data
```
Add `b4rrhh_minio_data` to the `volumes` section.

### MinIO bucket setup (one-time, done via console or mc CLI)
- Bucket name: `b4rrhh-employee-photos`
- Access policy: **public read** (anonymous GET allowed)
- CORS rule on the bucket: allow `PUT` from `http://localhost:4200` (and production origin when deployed)

### Backend config (`application.yml`)
```yaml
minio:
  endpoint: http://localhost:9000
  access-key: b4rrhh
  secret-key: b4rrhh123
  bucket-name: b4rrhh-employee-photos
  presigned-url-expiry-minutes: 10
```

---

## 2. Backend — `employee.photo` vertical

Follows hexagonal architecture in `com.b4rrhh.employee.photo`.

### Database
**`V83__add_employee_photo_url.sql`**
```sql
alter table employee add column if not exists photo_url varchar(512);
```

### Domain
**`Employee` model** — add nullable `photoUrl` field. `updatePhotoUrl(String photoUrl)` and `removePhotoUrl()` methods on the aggregate.

### Ports
**`EmployeePhotoStoragePort`** (secondary/outbound):
```java
public interface EmployeePhotoStoragePort {
    String generatePresignedPutUrl(String objectKey, int expiryMinutes);
    String buildPublicUrl(String objectKey);
    void deleteObject(String objectKey);
}
```

### Use Cases
Three use cases, each with interface + command + service implementation:

| Use Case | Command fields | Side effects |
|---|---|---|
| `GeneratePhotoUploadUrlUseCase` | employeeBusinessKey | Generates object key `photos/{rs}/{type}/{num}/{uuid}.jpg`, returns presigned PUT URL + key |
| `ConfirmEmployeePhotoUseCase` | employeeBusinessKey, objectKey | Reads current `photo_url`; if set, deletes old object from MinIO first. Builds public URL for new key, persists to `employee.photo_url` |
| `DeleteEmployeePhotoUseCase` | employeeBusinessKey | Deletes object from MinIO, sets `photo_url = null` |

Object key format: `photos/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/{uuid}.jpg`

### Infrastructure
**`MinioEmployeePhotoStorageAdapter`** implements `EmployeePhotoStoragePort` using `io.minio:minio` SDK. Injected with endpoint, access-key, secret-key, bucket-name from config.

### Web layer
**`EmployeePhotoController`** — path prefix `/employees/{ruleSystemCode}/{employeeTypeCode}/{employeeNumber}/photo` — role `ADMIN` on all three endpoints:

```
POST   /photo/upload-url   → 200 { uploadUrl: String, objectKey: String }
PUT    /photo              → 200 EmployeeResponse   (body: { objectKey: String })
DELETE /photo              → 204
```

`EmployeeResponse` gets new nullable field `photoUrl`.

OpenAPI spec updated with the three new endpoints and the `photoUrl` field on `EmployeeResponse`.

---

## 3. Frontend

### New dependency
```
npm install ngx-image-cropper
```

### `EmployeePhotoService`
`providedIn: 'root'` service that orchestrates the 3-step upload flow:
1. `POST /photo/upload-url` → receives `{ uploadUrl, objectKey }`
2. `PUT {uploadUrl}` via plain `HttpClient` with `Content-Type: image/jpeg` (bypasses the auth interceptor — MinIO URL is pre-authenticated)
3. `PUT /photo` with `{ objectKey }` → receives updated `EmployeeResponse`

Also exposes `deletePhoto(key)` → `DELETE /photo`.

### `EmployeePhotoUploadDialogComponent`
Standalone PrimeNG `p-dialog` component:
- Input: `employeeKey: EmployeeBusinessKey`, `visible: boolean`
- Output: `photoConfirmed: EventEmitter<string>` (emits new `photoUrl`), `cancelled: EventEmitter<void>`
- Contains `ngx-image-cropper` set to circular crop, output format JPEG, max output size 800×800px
- File input (hidden `<input type="file" accept="image/jpeg,image/png">`) triggered by a "Seleccionar archivo" button
- "Subir foto" button triggers: crop → upload flow via `EmployeePhotoService` → emits `photoConfirmed`
- Shows a spinner during upload; disables button on error and shows inline error message

### `EntityHeaderComponent` changes
- New input: `photoUrl: string | null` (default `null`)
- Avatar renders `<img [src]="photoUrl()" ...>` when `photoUrl` is set, falls back to `{{ avatarText() }}` initials
- `--entity-header-avatar-bg` token used for the fallback background (already defined)

### `EmployeeIdentityPanelComponent` changes
- Avatar div (currently shows initials) becomes clickable only when `isAdmin` is `true`
- On click: opens `EmployeePhotoUploadDialogComponent`
- On `photoConfirmed`: calls `EmployeeDetailStore.refreshEmployeeDetailByBusinessKey()` so the store's `selectedEmployeeDetail.photoUrl` updates and propagates to `EntityHeaderComponent`
- When photo exists: shows small "✕ Eliminar foto" button below avatar, calls `deletePhoto()`; on confirm resets `photoUrl` to `null`

### Auth interceptor bypass
The PUT to the MinIO presigned URL must **not** include the `Authorization` header (MinIO rejects it). The `authInterceptor` should skip URLs that don't start with `''` (relative — the current `BASE_PATH`). Since the MinIO URL is absolute (`http://localhost:9000/...`), the interceptor can check `req.url.startsWith('/')` — only inject the token for relative/same-origin requests.

---

## 4. Constraints and limits
- Accepted formats: JPEG, PNG (validated client-side before crop, MIME type check)
- Max input file size: 5 MB (validated before opening cropper, show error if exceeded)
- Output from cropper: JPEG, max 800×800px
- Presigned URL expiry: 10 minutes
- One photo per employee (replacing always deletes the previous object key first)

---

## 5. Out of scope
- Thumbnail generation / multiple resolutions
- Photo approval workflow
- Self-service (employees updating their own photo)
- Any role other than ADMIN changing photos
