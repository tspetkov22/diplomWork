package com.meditreat.repository;

import com.meditreat.model.TreatmentType;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TreatmentTypeRepository extends JpaRepository<TreatmentType, Long> {
    Optional<TreatmentType> findByNameAndLanguage(String name, String language);

    List<TreatmentType> findByLanguage(String language); // To get all types for a language

    Optional<TreatmentType> findByIdAndLanguage(Long id, String language);

    Optional<TreatmentType> findByNormalizedNameAndLanguage(String normalizedName, String language);

    boolean existsByNormalizedNameAndLanguage(String normalizedName, String language);
}