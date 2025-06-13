package com.meditreat.app.controller;

import com.meditreat.app.entity.Doctor;
import com.meditreat.app.service.DoctorService;
import com.meditreat.app.dto.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/doctors") // Base path for all doctor related APIs
public class DoctorController {

    private final DoctorService doctorService;

    @Autowired
    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    // Endpoint for doctor registration (can be used by login page registration)
    // Could also be POST /api/auth/register or similar if you build a dedicated
    // auth module
    @PostMapping("/register")
    public ResponseEntity<?> registerDoctor(@RequestBody Doctor doctor) {
        try {
            Doctor registeredDoctor = doctorService.registerDoctor(doctor);
            return new ResponseEntity<>(registeredDoctor, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginDoctor(@RequestBody LoginRequest loginRequest) {
        Optional<Doctor> doctorOptional = doctorService.loginDoctor(loginRequest.getEmail(),
                loginRequest.getPassword());
        if (doctorOptional.isPresent()) {
            // In a real app with sessions/JWT, you'd generate and return a token here.
            // For now, just return the doctor object.
            return ResponseEntity.ok(doctorOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }

    // Endpoint to get all doctors (for Admin page)
    @GetMapping
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        List<Doctor> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    // Endpoint to get a single doctor by ID (e.g., for editing)
    @GetMapping("/{id}")
    public ResponseEntity<?> getDoctorById(@PathVariable Long id) {
        return doctorService.getDoctorById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint to update a doctor (for Admin page)
    // Note: In a real app, you'd use a DTO (Data Transfer Object) here instead of
    // the entity directly
    // to control what fields can be updated and for better API design.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDoctor(@PathVariable Long id, @RequestBody Doctor doctorDetails) {
        try {
            Doctor updatedDoctor = doctorService.updateDoctor(id, doctorDetails);
            return ResponseEntity.ok(updatedDoctor);
        } catch (IllegalStateException e) {
            // Could be a not found or a conflict (e.g., email exists)
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Endpoint to delete a doctor (for Admin page)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDoctor(@PathVariable Long id) {
        try {
            doctorService.deleteDoctor(id);
            return ResponseEntity.noContent().build(); // Standard response for successful delete
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build(); // If doctor to delete is not found
        }
    }
}