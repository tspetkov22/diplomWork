package com.meditreat.app.controller;

import com.meditreat.app.dto.SymptomDto;
import com.meditreat.app.service.SymptomService;
import com.meditreat.model.Symptom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/symptoms")
public class SymptomController {

    private final SymptomService symptomService;

    @Autowired
    public SymptomController(SymptomService symptomService) {
        this.symptomService = symptomService;
    }

    @PostMapping
    public ResponseEntity<Symptom> createSymptom(@RequestBody Symptom symptom,
            @RequestHeader("Accept-Language") String language) {
        symptom.setLanguage(language.split(",")[0].split("-")[0]); // Extract primary language
        try {
            Symptom createdSymptom = symptomService.createSymptom(symptom);
            return new ResponseEntity<>(createdSymptom, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build(); // Or return error message
        }
    }

    @PostMapping("/{id}/translations")
    public ResponseEntity<Symptom> createTranslation(
            @PathVariable Long id,
            @RequestBody Symptom translationSymptom,
            @RequestHeader("Accept-Language") String language) {

        translationSymptom.setLanguage(language.split(",")[0].split("-")[0]); // Extract primary language

        try {
            Symptom createdTranslation = symptomService.createTranslation(id, translationSymptom);
            return new ResponseEntity<>(createdTranslation, HttpStatus.CREATED);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<Symptom>> getAllSymptoms() {
        List<Symptom> symptoms = symptomService.getAllSymptoms();
        return ResponseEntity.ok(symptoms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Symptom> getSymptomById(@PathVariable Long id) {
        return symptomService.getSymptomById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/translations/{targetLanguage}")
    public ResponseEntity<Symptom> getSymptomTranslation(
            @PathVariable Long id,
            @PathVariable String targetLanguage) {

        Optional<Symptom> translation = symptomService.findTranslation(id, targetLanguage);
        return translation
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Autocomplete endpoint
    @GetMapping("/search")
    public ResponseEntity<List<Symptom>> searchSymptoms(@RequestParam("name") String name,
            @RequestHeader("Accept-Language") String language) {
        String lang = language.split(",")[0].split("-")[0]; // Extract primary language
        List<Symptom> symptoms = symptomService.searchSymptomsByName(name, lang);
        if (symptoms.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(symptoms);
    }

    // Search with translations endpoint
    @GetMapping("/search-with-translations")
    public ResponseEntity<List<SymptomDto>> searchSymptomsWithTranslations(
            @RequestParam("name") String name,
            @RequestHeader("Accept-Language") String sourceLanguage,
            @RequestParam("targetLanguage") String targetLanguage) {

        String sourceLang = sourceLanguage.split(",")[0].split("-")[0]; // Extract primary language

        List<SymptomDto> symptomsWithTranslations = symptomService.searchSymptomsByNameWithTranslations(name,
                sourceLang, targetLanguage);

        if (symptomsWithTranslations.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(symptomsWithTranslations);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Symptom> updateSymptom(@PathVariable Long id, @RequestBody Symptom symptomDetails,
            @RequestHeader("Accept-Language") String language) {
        symptomDetails.setLanguage(language.split(",")[0].split("-")[0]); // Extract primary language
        try {
            Symptom updatedSymptom = symptomService.updateSymptom(id, symptomDetails);
            return ResponseEntity.ok(updatedSymptom);
        } catch (IllegalStateException e) {
            // Could be more specific, e.g., NOT_FOUND vs CONFLICT
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build(); // Or return error message
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSymptom(@PathVariable Long id) {
        try {
            symptomService.deleteSymptom(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.notFound().build();
        }
    }
}