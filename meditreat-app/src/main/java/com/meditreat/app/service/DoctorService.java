package com.meditreat.app.service;

import com.meditreat.app.entity.Doctor;
import com.meditreat.app.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DoctorService {

    private final DoctorRepository doctorRepository;

    @Autowired
    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Transactional
    public Doctor registerDoctor(Doctor doctor) {
        // Add any validation or pre-processing here if needed
        // For example, check if email or license number already exists
        if (doctorRepository.findByEmail(doctor.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + doctor.getEmail());
        }
        if (doctorRepository.findByLicenseNumber(doctor.getLicenseNumber()).isPresent()) {
            throw new IllegalStateException("License number already exists: " + doctor.getLicenseNumber());
        }
        // In a real application, hash the password here before saving
        // doctor.setPassword(passwordEncoder.encode(doctor.getPassword()));
        return doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public List<Doctor> getAllDoctors() {
        return doctorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> getDoctorById(Long id) {
        return doctorRepository.findById(id);
    }

    @Transactional
    public Doctor updateDoctor(Long id, Doctor doctorDetails) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Doctor not found with id: " + id));

        // Check for email uniqueness if it's being changed
        if (!doctor.getEmail().equals(doctorDetails.getEmail())
                && doctorRepository.findByEmail(doctorDetails.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already exists: " + doctorDetails.getEmail());
        }
        // Check for license number uniqueness if it's being changed
        if (!doctor.getLicenseNumber().equals(doctorDetails.getLicenseNumber())
                && doctorRepository.findByLicenseNumber(doctorDetails.getLicenseNumber()).isPresent()) {
            throw new IllegalStateException("License number already exists: " + doctorDetails.getLicenseNumber());
        }

        doctor.setFullName(doctorDetails.getFullName());
        doctor.setEmail(doctorDetails.getEmail());
        doctor.setSpecialization(doctorDetails.getSpecialization());
        doctor.setLicenseNumber(doctorDetails.getLicenseNumber());
        // Handle password update separately if needed, usually not part of general
        // update
        // If password can be updated here, ensure it's hashed.

        return doctorRepository.save(doctor);
    }

    @Transactional
    public void deleteDoctor(Long id) {
        if (!doctorRepository.existsById(id)) {
            throw new IllegalStateException("Doctor not found with id: " + id);
        }
        doctorRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Doctor> loginDoctor(String email, String password) {
        Optional<Doctor> doctorOptional = doctorRepository.findByEmail(email);
        if (doctorOptional.isPresent()) {
            Doctor doctor = doctorOptional.get();
            // Plain text password comparison - NOT FOR PRODUCTION
            if (doctor.getPassword().equals(password)) {
                return Optional.of(doctor);
            }
        }
        return Optional.empty();
    }

    // Add other business logic methods as needed
}