package com.meditreat.app.service;

import com.meditreat.app.dto.SymptomDto;
import com.meditreat.model.Symptom;
import com.meditreat.repository.SymptomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SymptomService {

    private final SymptomRepository symptomRepository;

    @Autowired
    public SymptomService(SymptomRepository symptomRepository) {
        this.symptomRepository = symptomRepository;
    }

    @Transactional
    public Symptom createSymptom(Symptom symptom) {
        // Check if a symptom with the same name and language already exists
        Optional<Symptom> existingSymptom = symptomRepository.findByNameIgnoreCaseAndLanguage(symptom.getName(),
                symptom.getLanguage());
        if (existingSymptom.isPresent()) {
            throw new IllegalStateException("Symptom with name '" + symptom.getName() + "' and language '"
                    + symptom.getLanguage() + "' already exists.");
        }

        // If no translation key is provided, generate a new one
        // This indicates this is the first symptom in a potential translation group
        if (symptom.getTranslationKey() == null || symptom.getTranslationKey().isEmpty()) {
            symptom.setTranslationKey(UUID.randomUUID().toString());
        }
        // Persist description
        symptom.setDescription(symptom.getDescription());

        return symptomRepository.save(symptom);
    }

    @Transactional
    public Symptom createTranslation(Long originalSymptomId, Symptom translationSymptom) {
        Symptom originalSymptom = symptomRepository.findById(originalSymptomId)
                .orElseThrow(
                        () -> new IllegalStateException("Original symptom not found with id: " + originalSymptomId));

        // Check if translation with same name and language already exists
        Optional<Symptom> existingSymptom = symptomRepository.findByNameIgnoreCaseAndLanguage(
                translationSymptom.getName(), translationSymptom.getLanguage());
        if (existingSymptom.isPresent()) {
            throw new IllegalStateException("Symptom with name '" + translationSymptom.getName() + "' and language '"
                    + translationSymptom.getLanguage() + "' already exists.");
        }

        // Set the translation key from the original symptom
        translationSymptom.setTranslationKey(originalSymptom.getTranslationKey());

        return symptomRepository.save(translationSymptom);
    }

    @Transactional(readOnly = true)
    public List<Symptom> getAllSymptoms() {
        return symptomRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Symptom> getSymptomById(Long id) {
        return symptomRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Symptom> searchSymptomsByName(String nameSubstring, String language) {
        return symptomRepository.findByNameStartingWithIgnoreCaseAndLanguage(nameSubstring, language);
    }

    @Transactional(readOnly = true)
    public List<SymptomDto> searchSymptomsByNameWithTranslations(String nameSubstring, String sourceLanguage,
            String targetLanguage) {
        List<Symptom> sourceSymptoms = symptomRepository.findByNameStartingWithIgnoreCaseAndLanguage(nameSubstring,
                sourceLanguage);

        return sourceSymptoms.stream().map(symptom -> {
            SymptomDto dto = toDto(symptom);

            // If the symptom has a translation key, look for matching symptoms in target
            // language
            if (symptom.getTranslationKey() != null && !symptom.getTranslationKey().isEmpty()) {
                List<Symptom> translations = symptomRepository.findByTranslationKey(symptom.getTranslationKey());
                // Find the translation in the target language
                Optional<Symptom> targetTranslation = translations.stream()
                        .filter(s -> s.getLanguage().equals(targetLanguage))
                        .findFirst();

                // Set the translation name if available
                targetTranslation.ifPresent(s -> dto.setTargetTranslation(s.getName()));
            }

            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Symptom> findTranslation(Long symptomId, String targetLanguage) {
        return symptomRepository.findById(symptomId).flatMap(symptom -> {
            if (symptom.getTranslationKey() == null || symptom.getTranslationKey().isEmpty()) {
                return Optional.empty();
            }

            List<Symptom> translations = symptomRepository.findByTranslationKey(symptom.getTranslationKey());
            return translations.stream()
                    .filter(s -> s.getLanguage().equals(targetLanguage))
                    .findFirst();
        });
    }

    @Transactional
    public Symptom updateSymptom(Long id, Symptom symptomDetails) {
        Symptom symptom = symptomRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Symptom not found with id: " + id));

        // Check if updating to a name and language that already exists for another
        // symptom
        Optional<Symptom> existingSymptomWithNewName = symptomRepository
                .findByNameIgnoreCaseAndLanguage(symptomDetails.getName(), symptomDetails.getLanguage());
        if (existingSymptomWithNewName.isPresent() && !existingSymptomWithNewName.get().getId().equals(id)) {
            throw new IllegalStateException("Another symptom with name '" + symptomDetails.getName()
                    + "' and language '" + symptomDetails.getLanguage() + "' already exists.");
        }

        symptom.setName(symptomDetails.getName());
        symptom.setLanguage(symptomDetails.getLanguage());

        // Update translation key if provided
        if (symptomDetails.getTranslationKey() != null) {
            symptom.setTranslationKey(symptomDetails.getTranslationKey());
        }
        // Update description if provided
        if (symptomDetails.getDescription() != null) {
            symptom.setDescription(symptomDetails.getDescription());
        }

        return symptomRepository.save(symptom);
    }

    @Transactional
    public void deleteSymptom(Long id) {
        if (!symptomRepository.existsById(id)) {
            throw new IllegalStateException("Symptom not found with id: " + id);
        }
        symptomRepository.deleteById(id);
    }

    // Convert Symptom entity to SymptomDto
    public SymptomDto toDto(Symptom symptom) {
        return new SymptomDto(
                symptom.getId(),
                symptom.getName(),
                symptom.getLanguage(),
                symptom.getTranslationKey(),
                symptom.getDescription());
    }
}