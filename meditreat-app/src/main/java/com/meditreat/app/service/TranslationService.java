package com.meditreat.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
public class TranslationService {
    private static final Logger logger = LoggerFactory.getLogger(TranslationService.class);

    // For a real implementation, you would inject these from properties
    // @Value("${translation.api.key}")
    private String apiKey = "YOUR_API_KEY"; // Replace with your actual API key in a real implementation

    private final RestTemplate restTemplate;

    public TranslationService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Translates text from one language to another.
     * This is a simplified implementation that simulates translation for
     * demonstration.
     * In production, you would connect to a real translation API.
     * 
     * @param text           The text to translate
     * @param sourceLanguage The source language code (e.g., "en", "bg")
     * @param targetLanguage The target language code (e.g., "en", "bg")
     * @return The translated text
     */
    public String translate(String text, String sourceLanguage, String targetLanguage) {
        if (text == null || text.trim().isEmpty()) {
            return text;
        }

        if (sourceLanguage.equals(targetLanguage)) {
            return text;
        }

        try {
            logger.info("Translating text from {} to {}: {}", sourceLanguage, targetLanguage, text);

            // This is a mock implementation for demonstration
            // In a real implementation, you would call an external translation API
            if (useSimulatedTranslation()) {
                return simulateTranslation(text, sourceLanguage, targetLanguage);
            } else {
                // Example of how you might call an external API like Google Translate
                // This is just an example and won't actually work without proper API key and
                // setup
                return callExternalTranslationApi(text, sourceLanguage, targetLanguage);
            }
        } catch (Exception e) {
            logger.error("Error translating text: {}", e.getMessage(), e);
            return text; // Return original text on error
        }
    }

    private boolean useSimulatedTranslation() {
        // For demo purposes, always use simulated translation
        return true;
    }

    private String simulateTranslation(String text, String sourceLanguage, String targetLanguage) {
        // This is a very simplified mock translation for demonstration
        // It only works with a few hardcoded words between English and Bulgarian

        Map<String, String> enToBgMap = new HashMap<>();
        enToBgMap.put("Common Cold", "Обикновена настинка");
        enToBgMap.put("Influenza", "Грип");
        enToBgMap.put("Headache", "Главоболие");
        enToBgMap.put("Fever", "Треска");
        enToBgMap.put("Medical", "Медицинско");
        enToBgMap.put("Homeopathic", "Хомеопатично");
        enToBgMap.put("Phytotherapeutic", "Фитотерапевтично");

        Map<String, String> bgToEnMap = new HashMap<>();
        for (Map.Entry<String, String> entry : enToBgMap.entrySet()) {
            bgToEnMap.put(entry.getValue(), entry.getKey());
        }

        if (sourceLanguage.equals("en") && targetLanguage.equals("bg")) {
            return enToBgMap.getOrDefault(text, text);
        } else if (sourceLanguage.equals("bg") && targetLanguage.equals("en")) {
            return bgToEnMap.getOrDefault(text, text);
        }

        return text;
    }

    private String callExternalTranslationApi(String text, String sourceLanguage, String targetLanguage) {
        try {
            // This is an example of how you might call Google Translate API
            // You would need to set up proper authentication and API key
            String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
            String url = "https://translation.googleapis.com/language/translate/v2?key=" + apiKey +
                    "&q=" + encodedText +
                    "&source=" + sourceLanguage +
                    "&target=" + targetLanguage;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            RequestEntity<?> request = new RequestEntity<>(headers, HttpMethod.GET, URI.create(url));
            ResponseEntity<Map> response = restTemplate.exchange(request, Map.class);

            // Parse the response from Google Translate
            // This would need to be adjusted based on the actual response structure
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Map<String, Object> data = (Map<String, Object>) responseBody.get("data");
                Map<String, Object> translations = (Map<String, Object>) data.get("translations");
                String translatedText = (String) translations.get("translatedText");
                return translatedText;
            }

            return text;
        } catch (Exception e) {
            logger.error("Error calling external translation API: {}", e.getMessage(), e);
            return text;
        }
    }
}