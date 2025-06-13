package com.meditreat.app.config;

import com.meditreat.model.Symptom;
import com.meditreat.app.service.SymptomService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Order(1) // Run before other initializers
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    private final SymptomService symptomService;

    public DataInitializer(SymptomService symptomService) {
        this.symptomService = symptomService;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info("Checking for predefined symptoms...");

        if (symptomService.getAllSymptoms().isEmpty()) {
            logger.info("No symptoms found. Creating predefined symptoms.");

            // Create symptom pairs with the same translationKey for translations
            String coldTranslationKey = "cold-translation-key";
            String fluTranslationKey = "flu-translation-key";

            Symptom symptom1En = new Symptom();
            symptom1En.setName("Common Cold");
            symptom1En.setLanguage("en");
            symptom1En.setTranslationKey(coldTranslationKey);
            symptom1En.setDescription(
                    "Common viral infection of the nose and throat. Symptoms include a runny nose, sore throat, cough, congestion, and mild body aches or a mild headache.");
            tryCreateSymptom(symptom1En, "Common Cold (en)");

            Symptom symptom1Bg = new Symptom();
            symptom1Bg.setName("Обикновена настинка");
            symptom1Bg.setLanguage("bg");
            symptom1Bg.setTranslationKey(coldTranslationKey);
            symptom1Bg.setDescription(
                    "Често срещана вирусна инфекция на носа и гърлото. Симптомите включват хрема, болки в гърлото, кашлица, запушване на носа и леки болки в тялото или леко главоболие.");
            tryCreateSymptom(symptom1Bg, "Обикновена настинка (bg)");

            Symptom symptom2En = new Symptom();
            symptom2En.setName("Influenza");
            symptom2En.setLanguage("en");
            symptom2En.setTranslationKey(fluTranslationKey);
            symptom2En.setDescription(
                    "A contagious respiratory illness caused by influenza viruses. Symptoms can be mild to severe and commonly include fever, runny nose, sore throat, muscle pain, headache, coughing, and fatigue.");
            tryCreateSymptom(symptom2En, "Influenza (en)");

            Symptom symptom2Bg = new Symptom();
            symptom2Bg.setName("Грип");
            symptom2Bg.setLanguage("bg");
            symptom2Bg.setTranslationKey(fluTranslationKey);
            symptom2Bg.setDescription(
                    "Заразна респираторна болест, причинена от грипни вируси. Симптомите могат да бъдат леки до тежки и обикновено включват треска, хрема, болки в гърлото, мускулни болки, главоболие, кашлица и умора.");
            tryCreateSymptom(symptom2Bg, "Грип (bg)");

            logger.info("Predefined symptoms creation process finished.");
        } else {
            logger.info("Symptoms already exist. Skipping creation of predefined symptoms.");
        }
    }

    private void tryCreateSymptom(Symptom symptom, String logName) {
        try {
            symptomService.createSymptom(symptom);
            logger.info("Created symptom: {}", logName);
        } catch (IllegalStateException e) {
            logger.info("Symptom {} already exists or another error: {}", logName, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error creating symptom {}: {}", logName, e.getMessage(), e);
        }
    }
}