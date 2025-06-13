package com.meditreat.app.repository;

import com.meditreat.app.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    // Custom query to find a doctor by email
    Optional<Doctor> findByEmail(String email);

    // Custom query to find a doctor by license number
    Optional<Doctor> findByLicenseNumber(String licenseNumber);

    // You can add more custom query methods here if needed
    // For example, to find doctors by specialization:
    // List<Doctor> findBySpecialization(String specialization);
}