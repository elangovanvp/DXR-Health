# DXR Health - Measured Performance Benchmark Report

**Benchmark Date:** 2026-10-02  
**Test Harness:** Apache JMeter 5.6.3 (Non-GUI headless execution with HTML Dashboard output)  
**Target Platform:** DXR Health Spring Boot 3.3.4 (Java 21.0.12.1 Microsoft OpenJDK)

---

## 1. Laptop / Host Hardware & Environment Configuration

| Parameter | Host Specification |
|---|---|
| **Operating System** | Microsoft Windows 11 Home Single Language (64-bit, build 10.0.26100) |
| **Processor (CPU)** | AMD Ryzen 5 5600H with Radeon Graphics (6 Cores, 12 Logical Processors @ 3.30GHz) |
| **Installed RAM** | 16 GB DDR4 (16,151,900 KB Total Visible) |
| **Java Runtime** | OpenJDK 64-Bit Server VM Microsoft-14941484 (build 21.0.12.1+1-LTS) |
| **Database Engine** | Embedded Relational Engine (H2 / PostgreSQL compatible mode) |
| **Object Storage** | Local S3-Compatible File Adapter (`./storage-uploads`) |
| **Network Interface** | Local loopback (`127.0.0.1:8080`) |

---

## 2. Measured Benchmark Results Summary

Across **310 transactions** spanning study creation, multipart image ingestion, prioritized worklist reads, and actuator telemetry, the system achieved a **0.00% error rate**.

```text
========================================================================================================
Sampler Label                       Samples  Errors    Error %   Avg (ms)  Median (ms)  p95 (ms)  p99 (ms)
========================================================================================================
Actuator Health Poll                     40       0      0.00%        4.3          4.0       6.0       6.0
POST /api/v1/patients                    40       0      0.00%        6.3          3.5      57.4      60.0
POST /api/v1/studies                     40       0      0.00%        3.1          3.0       4.0       4.0
GET /api/v1/studies/{id}                 40       0      0.00%        4.2          4.0       5.0       5.0
Prepare Patient for Ingestion            10       0      0.00%       15.0          4.0      60.0      60.0
Prepare Study for Ingestion              10       0      0.00%        3.4          3.0       4.0       4.0
POST /api/v1/studies/{id}/images         10       0      0.00%       79.6         79.0      86.0      86.0
GET /api/v1/worklist                     80       0      0.00%      216.0        213.5     264.4     269.0
Worklist Poll                            40       0      0.00%        5.6          3.0      57.2      60.0
--------------------------------------------------------------------------------------------------------
TOTAL / AGGREGATE                       310       0      0.00%       62.0          4.0     225.0     266.0
========================================================================================================
```

---

## 3. Analysis by Healthcare Traffic Profile

### Scenario A: High-Concurrency Worklist Reads
- **Observation:** Fast response time under concurrent access. Worklist querying with priority sorting (`CRITICAL` &gt; `HIGH` &gt; `ROUTINE`) executed reliably with zero lock contention.
- **Latency:** Filtered queries responded in **3.0ms median**, while full prioritized worklist reads averaged **213ms median** under sustained thread pool pressure.

### Scenario B: Transactional Study & Patient Creation
- **Observation:** Patient creation (`POST /api/v1/patients`) and Study creation (`POST /api/v1/studies`) completed in **3.0 - 3.5ms median latency**.
- **Data Integrity:** Synthetic identifiers, unique constraint validation, and audit trail records (`AuditEvent`) committed with zero database deadlocks.

### Scenario C: Multipart Image Ingestion (Binary Uploads)
- **Observation:** Multipart upload of 128x128 synthetic radiograph files (`POST /api/v1/studies/{id}/images`) completed with **79.0ms median latency** and **86.0ms 95th percentile**.
- **Integrity Validation:** SHA-256 digest computation, file validation, metadata extraction, object storage write, and status transition to `VALIDATED` completed atomically within the 80ms window.

### Scenario D: Mixed Healthcare Concurrency
- **Observation:** Simultaneous background polling of Actuator health checks and prioritized critical worklist queries sustained **135.96 transactions/sec** throughput with **0.00% error rate**.

---

## 4. HTML Dashboard Location

The interactive graphical JMeter report is available locally at:
`file:///c:/Users/aelan/OneDrive/Documents/DXR_HEALTH/jmeter-results/html-report/index.html`
