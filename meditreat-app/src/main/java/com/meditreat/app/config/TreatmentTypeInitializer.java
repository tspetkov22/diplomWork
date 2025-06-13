package com.meditreat.app.config;

import com.meditreat.model.TreatmentType;
import com.meditreat.repository.TreatmentTypeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(3) // Run after DataInitializer and DoctorInitializer
public class TreatmentTypeInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(TreatmentTypeInitializer.class);

    private final TreatmentTypeRepository treatmentTypeRepository;

    public TreatmentTypeInitializer(TreatmentTypeRepository treatmentTypeRepository) {
        this.treatmentTypeRepository = treatmentTypeRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking for predefined treatment types...");

        if (treatmentTypeRepository.count() == 0) {
            logger.info("No treatment types found. Creating predefined treatment types.");

            // Create treatment types with consistent names
            createTreatmentTypePair("MEDICAL", "МЕДИЦИНСКО");
            createTreatmentTypePair("HOMEOPATHIC", "ХОМЕОПАТИЧНО");
            createTreatmentTypePair("PHYTOTHERAPEUTIC", "ФИТОТЕРАПЕВТИЧНО");

            logger.info("Predefined treatment types created successfully.");
        } else {
            logger.info("Treatment types already exist. Skipping creation of predefined types.");
        }
    }

    private void createTreatmentTypePair(String englishName, String bulgarianName) {
        // Create English version
        TreatmentType englishType = new TreatmentType();
        englishType.setName(englishName);
        englishType.setLanguage("en");
        englishType = treatmentTypeRepository.save(englishType);

        // Create Bulgarian version with the same normalized name
        TreatmentType bulgarianType = new TreatmentType();
        bulgarianType.setName(bulgarianName);
        bulgarianType.setLanguage("bg");
        bulgarianType.setNormalizedName(englishType.getName()); // Use English name as normalized name
        treatmentTypeRepository.save(bulgarianType);

        logger.info("Created treatment type pair: {} (EN) - {} (BG)", englishName, bulgarianName);
    }

    private void createTreatmentType(String name, String language) {
        TreatmentType type = new TreatmentType();
        type.setName(name);
        type.setLanguage(language);
        treatmentTypeRepository.save(type);
        logger.info("Created treatment type: {} ({})", name, language);
    }
}