# DXR Health - JMeter Load Test Guide & Benchmark Plan

This directory contains the Apache JMeter performance testing specifications designed to evaluate realistic hospital-scale concurrency for the DXR Health platform.

---

## Load Profiles & Scenarios

| Scenario | Target Endpoint | Concurrency Profile | Primary Metric Target |
|---|---|---|---|
| **Scenario A: Worklist Read** | `GET /api/v1/worklist` | 100 virtual users, 30 s ramp-up, 10 loops | p50 < 30ms, p95 < 80ms, 0% error rate |
| **Scenario B: Study Create / Read** | `POST /api/v1/studies` & `GET /api/v1/studies/{id}` | 50 virtual users, 30 s ramp-up, 10 loops | Transactional DB throughput, sub-50ms latency |
| **Scenario C: Image Ingestion** | `POST /api/v1/studies/{id}/images` | 25 concurrent uploads, synthetic 1-2MB files | Upload latency, storage buffer stability |
| **Scenario D: Mixed Workflow** | Weighted reads/writes/uploads | 60 virtual users with mixed operations | End-to-end service stability under sustained load |

---

## How to Execute the Benchmark

### 1. Start the Backend Service
```bash
# Terminal 1: Start backend
cd backend
mvn spring-boot:run
```

### 2. Run Headless JMeter Test with HTML Dashboard Generation
```bash
jmeter -n -t DXR-Health-Load-Test.jmx -l jmeter-results/results.jtl -e -o jmeter-results/html-report/
```

### 3. Inspect Measured Outputs
Open `jmeter-results/html-report/index.html` in any browser to review latency distributions (p50, p90, p95, p99), throughput curves (requests/sec), and HTTP response codes.
