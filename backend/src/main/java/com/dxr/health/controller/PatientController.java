package com.dxr.health.controller;

import com.dxr.health.dto.CreatePatientRequest;
import com.dxr.health.model.ImagingStudy;
import com.dxr.health.model.Patient;
import com.dxr.health.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @PostMapping
    public ResponseEntity<Patient> createPatient(@Valid @RequestBody CreatePatientRequest request) {
        Patient created = patientService.createPatient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{patientId}")
    public ResponseEntity<Patient> getPatient(@PathVariable("patientId") String patientId) {
        Patient patient = patientService.getPatient(patientId);
        return ResponseEntity.ok(patient);
    }

    @GetMapping
    public ResponseEntity<List<Patient>> listPatients() {
        List<Patient> patients = patientService.listAllPatients();
        return ResponseEntity.ok(patients);
    }

    @GetMapping("/{patientId}/studies")
    public ResponseEntity<List<ImagingStudy>> getPatientStudies(@PathVariable("patientId") String patientId) {
        List<ImagingStudy> studies = patientService.getPatientStudies(patientId);
        return ResponseEntity.ok(studies);
    }
}
