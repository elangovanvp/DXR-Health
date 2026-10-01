package com.dxr.health.repository;

import com.dxr.health.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    Optional<Patient> findBySyntheticReference(String syntheticReference);
    boolean existsBySyntheticReference(String syntheticReference);
}
