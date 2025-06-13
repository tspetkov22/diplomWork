package com.meditreat.repository;

import com.meditreat.model.Symptom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SymptomRepository extends JpaRepository<Symptom, Long> {
    Optional<Symptom> findByNameIgnoreCaseAndLanguage(String name, String language);

    List<Symptom> findByNameStartingWithIgnoreCaseAndLanguage(String prefix, String language);

    List<Symptom> findByTranslationKey(String translationKey);
}