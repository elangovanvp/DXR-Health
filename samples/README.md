# DXR Health - Sample Synthetic Imaging Assets

This directory contains strictly synthetic, non-clinical test assets created specifically for automated testing, API validation (Postman), and load testing (JMeter).

---

## Files

1. `synthetic_chest_dx.png`: Synthetic grayscale gradient image representing a Digital Radiography (DX) chest projection.
2. `synthetic_chest_cr.dcm`: Synthetic DICOM object containing standard 128-byte preamble, `DICM` magic header, Digital X-Ray SOP Class UID (`1.2.840.10008.5.1.4.1.1.1.1`), `DX` modality tag, synthetic Patient Name, and `1024x1024` matrix dimensions.

---

## Safety & Ethics Statement

- **Zero PHI**: These assets contain zero real patient identifiers or protected health information (PHI).
- **Educational Prototype**: All metadata is programmatically generated for unit and integration testing.
- **Do Not Use for Clinical Diagnosis**: These test patterns are not real anatomical scans and must never be used for clinical decision-making.
