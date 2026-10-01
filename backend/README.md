# DXR Health - Backend REST Service

The DXR Health backend is a production-ready, cloud-native Spring Boot service responsible for diagnostic X-ray ingestion, validation, S3/MinIO object storage, model-agnostic AI triage orchestration, priority worklist querying, and radiologist reporting.

---

## Architecture & Layers

- `controller/`: REST API Controllers handling HTTP serialization, validation, status codes, and routing.
- `service/`: Core healthcare domain logic, workflow orchestration, duplicate detection, and verification rules.
- `ai/`: Pluggable AI inference adapter layer (`TriageModelAdapter`) with mock, rule-based, and remote inference implementations.
- `storage/`: S3/MinIO object storage client with automatic fallback for local development.
- `model/`: Durable JPA entities mapping clinical concepts (`Patient`, `ImagingStudy`, `ImageAsset`, `TriageResult`, `DiagnosticReport`, `AuditEvent`).
- `repository/`: Spring Data JPA repositories with custom query methods for prioritized worklists.
- `dto/`: Request/Response records with Bean Validation constraints.
- `exception/`: Centralized HTTP exception handling returning standardized error responses.
- `config/`: CORS, Jackson, Security, and Storage configurations.

---

## Configuration & Environment Variables

Copy `.env.example` to `.env` or set the following variables:

| Variable | Default | Purpose |
|---|---|---|
| `SERVER_PORT` | `8080` | Port for the Spring Boot REST API |
| `SPRING_PROFILES_ACTIVE` | `dev` | Active Spring profile (`dev`, `docker`, `test`) |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/dxr_health` | Database connection URL |
| `SPRING_DATASOURCE_USERNAME` | `dxr_user` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | `dxr_secure_password_2026` | Database password |
| `STORAGE_S3_ENDPOINT` | `http://localhost:9000` | S3 / MinIO endpoint |
| `STORAGE_S3_ACCESS_KEY` | `dxr_minio_admin` | MinIO Access Key |
| `STORAGE_S3_SECRET_KEY` | `dxr_minio_secret_key_2026` | MinIO Secret Key |
| `STORAGE_S3_BUCKET` | `dxr-xray-images` | Storage bucket name |
| `AI_ADAPTER_TYPE` | `mock` | Triage adapter mode (`mock`, `remote`, `rule-based`) |

---

## Local Development

### 1. Build and Run
```bash
mvn clean install
mvn spring-boot:run
```

### 2. Run Test Suite
```bash
mvn test
```

### 3. Health Endpoint
```bash
curl http://localhost:8080/actuator/health
```
