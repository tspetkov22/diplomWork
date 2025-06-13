package com.meditreat.app.config;

import com.meditreat.app.entity.Doctor;
import com.meditreat.app.repository.DoctorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2) // Run after DataInitializer (if it has @Order(1))
public class DoctorInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DoctorInitializer.class);

    private final DoctorRepository doctorRepository;

    public DoctorInitializer(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking for test doctor account...");

        if (doctorRepository.findByEmail("test@example.com").isEmpty()) {
            logger.info("Creating test doctor account");

            Doctor testDoctor = new Doctor(
                    "Test Doctor",
                    "test@example.com",
                    "General Medicine",
                    "TEST123",
                    "password123");

            doctorRepository.save(testDoctor);
            logger.info("Test doctor account created successfully");
        } else {
            logger.info("Test doctor account already exists");
        }
    }
}